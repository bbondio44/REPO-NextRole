package com.nextrole.explainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextrole.assessment.AssessmentAttemptService;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.AssessmentAttemptStatus;
import com.nextrole.data.ExplainabilityDiagnostic;
import com.nextrole.data.ExplainabilityDiagnosticRepository;
import com.nextrole.data.ExplainabilityDiagnosticStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Coordinates only the first generation. Read/retry HTTP operations are introduced in a later checkpoint. */
@Service
public class ExplainabilityService {
    private final AssessmentAttemptService attempts;
    private final ExplainabilityDiagnosticRepository diagnostics;
    private final ExplainabilityContextService contexts;
    private final DiagnosisGenerator generator;
    private final ExplainabilityPromptCatalog prompts;
    private final DiagnosisValidator validator;
    private final ExplainabilityProperties properties;
    private final ObjectMapper mapper;

    public ExplainabilityService(AssessmentAttemptService attempts,
                                 ExplainabilityDiagnosticRepository diagnostics,
                                 ExplainabilityContextService contexts,
                                 DiagnosisGenerator generator,
                                 ExplainabilityPromptCatalog prompts,
                                 DiagnosisValidator validator,
                                 ExplainabilityProperties properties,
                                 ObjectMapper mapper) {
        this.attempts = attempts;
        this.diagnostics = diagnostics;
        this.contexts = contexts;
        this.generator = generator;
        this.prompts = prompts;
        this.validator = validator;
        this.properties = properties;
        this.mapper = mapper;
    }

    public GenerationOutcome generateInitial(UUID userId, UUID attemptId, String roleId) {
        var attempt = attempts.owned(userId, attemptId);
        if (attempt.status != AssessmentAttemptStatus.COMPLETED) {
            throw new ApiProblem(HttpStatus.CONFLICT, "ATTEMPT_NOT_COMPLETED",
                    "Solo se puede generar un diagnóstico para una evaluación completada");
        }

        var existing = diagnostics.findByAttemptIdAndRoleId(attemptId, roleId);
        if (existing.isPresent()) return outcome(existing.get());

        ExplainabilityPromptCatalog.PromptResource prompt = prompts.load(properties.getPromptVersion());
        ExplainabilityContext context = contexts.build(userId, attemptId, roleId);
        String contextHash = context.sha256(mapper);
        UUID generationToken = UUID.randomUUID();
        int claimed = diagnostics.createGenerating(UUID.randomUUID(), attemptId, roleId, generationToken,
                Instant.now().plus(properties.getLease()), context.roleName(), context.roleDescription());
        if (claimed == 0) {
            return diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                    .map(this::outcome)
                    .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
        }

        final DiagnosisGenerator.GenerationResult generated;
        try {
            // This call is deliberately outside any transaction or database lock.
            generated = generator.generate(context, prompt);
        } catch (DiagnosisGenerator.GenerationException e) {
            return fail(attemptId, roleId, generationToken, e.failureCode().name());
        } catch (RuntimeException e) {
            return fail(attemptId, roleId, generationToken, DiagnosisGenerator.FailureCode.PROVIDER_ERROR.name());
        }

        final DiagnosisValidator.ValidatedDiagnosis diagnosis;
        try {
            Set<String> competencyIds = context.competencies().stream()
                    .map(ExplainabilityCompetencyContext::id).collect(Collectors.toUnmodifiableSet());
            diagnosis = validator.validate(generated.rawResponse(), competencyIds);
        } catch (IllegalArgumentException e) {
            return fail(attemptId, roleId, generationToken,
                    DiagnosisGenerator.FailureCode.INVALID_PROVIDER_RESPONSE.name());
        }

        DiagnosisGenerator.Usage usage = generated.usage();
        int completed = diagnostics.completeSuccess(attemptId, roleId, generationToken, diagnosis.toJson(mapper),
                generated.provider(), generated.modelRequested(), generated.modelServed(), prompt.version(),
                prompt.sha256(), contextHash, generated.durationMs(), usage == null ? null : usage.inputTokens(),
                usage == null ? null : usage.outputTokens(), usage == null ? null : usage.reasoningTokens(),
                usage == null ? null : usage.cost(), context.roleName(), context.roleDescription());
        if (completed == 1) {
            return new GenerationOutcome(ExplainabilityDiagnosticStatus.SUCCEEDED, context.roleName(),
                    context.roleDescription(), diagnosis, generated.modelRequested(), generated.modelServed(),
                    prompt.version(), null);
        }
        return diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                .map(this::outcome)
                .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
    }

    private GenerationOutcome fail(UUID attemptId, String roleId, UUID generationToken, String failureCode) {
        int updated = diagnostics.completeFailure(attemptId, roleId, generationToken, failureCode);
        if (updated == 1) return new GenerationOutcome(ExplainabilityDiagnosticStatus.FAILED,
                null, null, null, null, null, null, failureCode);
        return diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                .map(this::outcome)
                .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
    }

    private GenerationOutcome outcome(ExplainabilityDiagnostic diagnostic) {
        DiagnosisValidator.ValidatedDiagnosis diagnosis = null;
        if (diagnostic.status == ExplainabilityDiagnosticStatus.SUCCEEDED) {
            try {
                diagnosis = mapper.treeToValue(diagnostic.diagnosisContent, DiagnosisValidator.ValidatedDiagnosis.class);
            } catch (Exception e) {
                throw inconsistentDiagnosticState();
            }
        }
        return new GenerationOutcome(diagnostic.status, diagnostic.roleNameSnapshot, diagnostic.roleDescriptionSnapshot,
                diagnosis, diagnostic.modelRequested, diagnostic.modelServed, diagnostic.promptVersion,
                diagnostic.failureCode);
    }

    private static ApiProblem inconsistentDiagnosticState() {
        return new ApiProblem(HttpStatus.CONFLICT, "INCONSISTENT_DIAGNOSTIC_STATE",
                "No se pudo recuperar el estado del diagnóstico");
    }

    public record GenerationOutcome(ExplainabilityDiagnosticStatus status, String roleName,
                                    String roleDescription, DiagnosisValidator.ValidatedDiagnosis diagnosis,
                                    String modelRequested, String modelServed, String promptVersion,
                                    String failureCode) {}
}
