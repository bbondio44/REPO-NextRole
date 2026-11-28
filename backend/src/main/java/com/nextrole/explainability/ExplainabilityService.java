package com.nextrole.explainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextrole.assessment.AssessmentAttemptService;
import com.nextrole.assessment.RoleCatalogService;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.AssessmentAttempt;
import com.nextrole.data.AssessmentAttemptStatus;
import com.nextrole.data.ExplainabilityDiagnostic;
import com.nextrole.data.ExplainabilityDiagnosticRepository;
import com.nextrole.data.ExplainabilityDiagnosticStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Coordinates synchronous diagnosis generation without holding a database transaction during provider I/O. */
@Service
public class ExplainabilityService {
    private static final Logger log = LoggerFactory.getLogger(ExplainabilityService.class);
    private final AssessmentAttemptService attempts;
    private final RoleCatalogService roles;
    private final ExplainabilityDiagnosticRepository diagnostics;
    private final ExplainabilityContextService contexts;
    private final DiagnosisGenerator generator;
    private final ExplainabilityPromptCatalog prompts;
    private final DiagnosisValidator validator;
    private final ExplainabilityProperties properties;
    private final ObjectMapper mapper;

    public ExplainabilityService(AssessmentAttemptService attempts, RoleCatalogService roles,
                                 ExplainabilityDiagnosticRepository diagnostics,
                                 ExplainabilityContextService contexts, DiagnosisGenerator generator,
                                 ExplainabilityPromptCatalog prompts, DiagnosisValidator validator,
                                 ExplainabilityProperties properties, ObjectMapper mapper) {
        this.attempts = attempts;
        this.roles = roles;
        this.diagnostics = diagnostics;
        this.contexts = contexts;
        this.generator = generator;
        this.prompts = prompts;
        this.validator = validator;
        this.properties = properties;
        this.mapper = mapper;
    }

    /** Pure read: it never updates an expired lease or calls the provider. */
    public GenerationOutcome getStatus(UUID userId, UUID attemptId, String roleId) {
        validateAccess(userId, attemptId, roleId);
        return diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                .map(diagnostic -> outcome(diagnostic, Instant.now()))
                .orElseGet(() -> notStarted(attemptId, roleId));
    }

    /** Starts only an absent pair. Existing failure always requires the explicit retry operation. */
    public GenerationOutcome generateInitial(UUID userId, UUID attemptId, String roleId) {
        validateAccess(userId, attemptId, roleId);
        Optional<ExplainabilityDiagnostic> existing = diagnostics.findByAttemptIdAndRoleId(attemptId, roleId);
        if (existing.isPresent()) return existingGenerationOutcome(existing.get());

        ExplainabilityContext context = contexts.build(userId, attemptId, roleId);
        ExplainabilityPromptCatalog.PromptResource prompt = prompts.load(properties.getPromptVersion());
        UUID generationToken = UUID.randomUUID();
        int claimed = diagnostics.createGenerating(UUID.randomUUID(), attemptId, roleId, generationToken,
                Instant.now().plus(properties.getLease()), context.roleName(), context.roleDescription());
        if (claimed == 0) {
            ExplainabilityDiagnostic winner = diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                    .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
            return existingGenerationOutcome(winner);
        }
        return invokeClaimed(userId, attemptId, roleId, generationToken, context, prompt);
    }

    /** Explicit user retry; only FAILED or expired GENERATING rows can be claimed. */
    public GenerationOutcome retryManual(UUID userId, UUID attemptId, String roleId) {
        validateAccess(userId, attemptId, roleId);
        ExplainabilityDiagnostic existing = diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                .orElseThrow(() -> new ApiProblem(HttpStatus.CONFLICT, "DIAGNOSIS_NOT_STARTED",
                        "Todavía no existe un diagnóstico para reintentar"));
        if (existing.status == ExplainabilityDiagnosticStatus.SUCCEEDED) {
            throw new ApiProblem(HttpStatus.CONFLICT, "DIAGNOSIS_ALREADY_SUCCEEDED",
                    "El diagnóstico ya fue generado correctamente");
        }

        Instant now = Instant.now();
        boolean failed = existing.status == ExplainabilityDiagnosticStatus.FAILED;
        boolean expired = existing.status == ExplainabilityDiagnosticStatus.GENERATING
                && (existing.leaseUntil == null || !existing.leaseUntil.isAfter(now));
        if (!failed && !expired) return outcome(existing, now);

        ExplainabilityContext context = contexts.build(userId, attemptId, roleId);
        ExplainabilityPromptCatalog.PromptResource prompt = prompts.load(properties.getPromptVersion());
        UUID generationToken = UUID.randomUUID();
        Instant nextLease = now.plus(properties.getLease());
        int claimed = failed
                ? diagnostics.claimFailed(attemptId, roleId, generationToken, nextLease)
                : diagnostics.claimExpired(attemptId, roleId, generationToken, nextLease, now);
        if (claimed == 0) {
            ExplainabilityDiagnostic winner = diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                    .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
            if (winner.status == ExplainabilityDiagnosticStatus.SUCCEEDED) {
                throw new ApiProblem(HttpStatus.CONFLICT, "DIAGNOSIS_ALREADY_SUCCEEDED",
                        "El diagnóstico ya fue generado correctamente");
            }
            return outcome(winner, Instant.now());
        }
        return invokeClaimed(userId, attemptId, roleId, generationToken, context, prompt);
    }

    private void validateAccess(UUID userId, UUID attemptId, String roleId) {
        AssessmentAttempt attempt = attempts.owned(userId, attemptId);
        if (attempt.status != AssessmentAttemptStatus.COMPLETED) {
            throw new ApiProblem(HttpStatus.CONFLICT, "ATTEMPT_NOT_COMPLETED",
                    "Solo se puede consultar un diagnóstico para una evaluación completada");
        }
        roles.one(roleId);
    }

    private GenerationOutcome invokeClaimed(UUID userId, UUID attemptId, String roleId, UUID generationToken,
                                            ExplainabilityContext context,
                                            ExplainabilityPromptCatalog.PromptResource prompt) {
        String contextHash = context.sha256(mapper);
        final DiagnosisGenerator.GenerationResult generated;
        try {
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
        } catch (DiagnosisValidator.ValidationException e) {
            return fail(attemptId, roleId, generationToken, e.failureCode().name());
        } catch (IllegalArgumentException e) {
            return fail(attemptId, roleId, generationToken,
                    DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_SCHEMA.name());
        }

        DiagnosisGenerator.Usage usage = generated.usage();
        int completed = diagnostics.completeSuccess(attemptId, roleId, generationToken, diagnosis.toJson(mapper),
                generated.provider(), generated.modelRequested(), generated.modelServed(), prompt.version(),
                prompt.sha256(), contextHash, generated.durationMs(), usage == null ? null : usage.inputTokens(),
                usage == null ? null : usage.outputTokens(), usage == null ? null : usage.reasoningTokens(),
                usage == null ? null : usage.cost(), context.roleName(), context.roleDescription());
        if (completed == 1) return new GenerationOutcome(ExplainabilityDiagnosticStatus.SUCCEEDED,
                context.roleName(), context.roleDescription(), diagnosis, generated.modelRequested(),
                generated.modelServed(), prompt.version(), null, Instant.now(), null, null);
        return diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                .map(diagnostic -> outcome(diagnostic, Instant.now()))
                .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
    }

    private GenerationOutcome fail(UUID attemptId, String roleId, UUID generationToken, String failureCode) {
        log.warn("Explainability generation failed: category={}", failureCode);
        int updated = diagnostics.completeFailure(attemptId, roleId, generationToken, failureCode);
        if (updated == 1) return new GenerationOutcome(ExplainabilityDiagnosticStatus.FAILED,
                null, null, null, null, null, null, failureCode, null, null, true);
        return diagnostics.findByAttemptIdAndRoleId(attemptId, roleId)
                .map(diagnostic -> outcome(diagnostic, Instant.now()))
                .orElseThrow(ExplainabilityService::inconsistentDiagnosticState);
    }

    private GenerationOutcome existingGenerationOutcome(ExplainabilityDiagnostic diagnostic) {
        GenerationOutcome result = outcome(diagnostic, Instant.now());
        if (result.status() == ExplainabilityDiagnosticStatus.FAILED) {
            throw new ApiProblem(HttpStatus.CONFLICT, "DIAGNOSIS_RETRY_REQUIRED",
                    "El diagnóstico requiere un reintento manual");
        }
        return result;
    }

    private GenerationOutcome outcome(ExplainabilityDiagnostic diagnostic, Instant now) {
        if (diagnostic.status == ExplainabilityDiagnosticStatus.GENERATING) {
            boolean expired = diagnostic.leaseUntil == null || !diagnostic.leaseUntil.isAfter(now);
            if (expired) return new GenerationOutcome(ExplainabilityDiagnosticStatus.FAILED,
                    null, null, null, null, null, null, "GENERATION_INTERRUPTED", null, null, true);
            return new GenerationOutcome(ExplainabilityDiagnosticStatus.GENERATING, null, null, null,
                    null, null, null, null, null, Math.max(1, Math.toIntExact(properties.getPollInterval().toSeconds())), null);
        }
        if (diagnostic.status == ExplainabilityDiagnosticStatus.FAILED) {
            return new GenerationOutcome(ExplainabilityDiagnosticStatus.FAILED, null, null, null,
                    null, null, null, diagnostic.failureCode, null, null, true);
        }
        DiagnosisValidator.ValidatedDiagnosis diagnosis;
        try {
            diagnosis = mapper.treeToValue(diagnostic.diagnosisContent, DiagnosisValidator.ValidatedDiagnosis.class);
        } catch (Exception e) {
            throw inconsistentDiagnosticState();
        }
        return new GenerationOutcome(ExplainabilityDiagnosticStatus.SUCCEEDED, diagnostic.roleNameSnapshot,
                diagnostic.roleDescriptionSnapshot, diagnosis, diagnostic.modelRequested, diagnostic.modelServed,
                diagnostic.promptVersion, null, diagnostic.succeededAt, null, null);
    }

    private GenerationOutcome notStarted(UUID attemptId, String roleId) {
        return new GenerationOutcome(null, null, null, null, null, null, null, null, null, null, null,
                attemptId, roleId, "NOT_STARTED");
    }

    private static ApiProblem inconsistentDiagnosticState() {
        return new ApiProblem(HttpStatus.CONFLICT, "INCONSISTENT_DIAGNOSTIC_STATE",
                "No se pudo recuperar el estado del diagnóstico");
    }

    public record GenerationOutcome(ExplainabilityDiagnosticStatus status, String roleName,
                                    String roleDescription, DiagnosisValidator.ValidatedDiagnosis diagnosis,
                                    String modelRequested, String modelServed, String promptVersion,
                                    String failureCode, Instant generatedAt, Integer retryAfterSeconds,
                                    Boolean canRetry, UUID attemptId, String roleId, String explicitStatus) {
        public GenerationOutcome(ExplainabilityDiagnosticStatus status, String roleName, String roleDescription,
                                 DiagnosisValidator.ValidatedDiagnosis diagnosis, String modelRequested,
                                 String modelServed, String promptVersion, String failureCode,
                                 Instant generatedAt, Integer retryAfterSeconds, Boolean canRetry) {
            this(status, roleName, roleDescription, diagnosis, modelRequested, modelServed, promptVersion,
                    failureCode, generatedAt, retryAfterSeconds, canRetry, null, null, null);
        }

        public String statusValue() {
            return explicitStatus != null ? explicitStatus : status.name();
        }
    }
}
