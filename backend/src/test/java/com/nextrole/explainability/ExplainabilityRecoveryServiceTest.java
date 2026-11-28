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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExplainabilityRecoveryServiceTest {
    private static final UUID USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ATTEMPT_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final String ROLE_ID = "qa-jr";
    private static final String VALID = "{\"currentSituation\":\"Actual\",\"recommendedFocus\":\"Focus\","
            + "\"importanceExplanation\":\"Why\",\"professionalRelevance\":\"Work\","
            + "\"highlightedCompetencyIds\":[\"api-testing\"]}";

    @Mock AssessmentAttemptService attempts;
    @Mock RoleCatalogService roles;
    @Mock ExplainabilityDiagnosticRepository diagnostics;
    @Mock ExplainabilityContextService contexts;
    @Mock DiagnosisGenerator generator;
    private ExplainabilityService service;
    private ExplainabilityContext context;

    @BeforeEach
    void setUp() {
        var properties = new ExplainabilityProperties();
        context = new ExplainabilityContext(ROLE_ID, "QA", "Rol QA", BigDecimal.valueOf(70), List.of(
                new ExplainabilityCompetencyContext("api-testing", "Pruebas de APIs",
                        new ExplainabilityCompetencyContext.LevelContext(1, "Comprendo pruebas"),
                        new ExplainabilityCompetencyContext.LevelContext(2, "Puedo probar"),
                        ExplainabilityCompetencyContext.Classification.ESSENTIAL, 1, 2)));
        service = new ExplainabilityService(attempts, roles, diagnostics, contexts, generator,
                new ExplainabilityPromptCatalog(), new DiagnosisValidator(new ObjectMapper()), properties,
                new ObjectMapper());
        when(attempts.owned(USER_ID, ATTEMPT_ID)).thenReturn(new AssessmentAttempt(ATTEMPT_ID, USER_ID,
                AssessmentAttemptStatus.COMPLETED, UUID.randomUUID(), UUID.randomUUID(), Instant.EPOCH));
        when(roles.one(ROLE_ID)).thenReturn(new RoleCatalogService.RoleDescription(ROLE_ID, "QA", "Rol QA"));
    }

    @Test
    void getStatusDerivesExpiredGenerationAsRetryableWithoutWriting() {
        ExplainabilityDiagnostic expired = diagnostic(ExplainabilityDiagnosticStatus.GENERATING);
        expired.leaseUntil = Instant.now().minusSeconds(1);
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.of(expired));

        var outcome = service.getStatus(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.FAILED, outcome.status());
        assertEquals("GENERATION_INTERRUPTED", outcome.failureCode());
        assertTrue(outcome.canRetry());
        verify(diagnostics).findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID);
        verifyNoMoreInteractions(diagnostics);
        verifyNoInteractions(contexts, generator);
    }

    @Test
    void retryRequiresAnExistingFailedOrExpiredDiagnostic() {
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.empty());

        ApiProblem error = assertThrows(ApiProblem.class, () -> service.retryManual(USER_ID, ATTEMPT_ID, ROLE_ID));

        assertEquals("DIAGNOSIS_NOT_STARTED", error.code());
        verifyNoInteractions(contexts, generator);
        verify(diagnostics, never()).claimFailed(any(), anyString(), any(), any());
        verify(diagnostics, never()).claimExpired(any(), anyString(), any(), any(), any());
    }

    @Test
    void retryDoesNotClaimOrCallProviderWhileGenerationLeaseIsActive() {
        ExplainabilityDiagnostic active = diagnostic(ExplainabilityDiagnosticStatus.GENERATING);
        active.leaseUntil = Instant.now().plusSeconds(30);
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.of(active));

        var outcome = service.retryManual(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.GENERATING, outcome.status());
        assertEquals(2, outcome.retryAfterSeconds());
        verify(diagnostics, never()).claimFailed(any(), anyString(), any(), any());
        verify(diagnostics, never()).claimExpired(any(), anyString(), any(), any(), any());
        verifyNoInteractions(contexts, generator);
    }

    @Test
    void retryClaimsFailedRowThenValidatesAndPersistsSuccess() {
        ExplainabilityDiagnostic failed = diagnostic(ExplainabilityDiagnosticStatus.FAILED);
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.of(failed));
        when(contexts.build(USER_ID, ATTEMPT_ID, ROLE_ID)).thenReturn(context);
        when(diagnostics.claimFailed(eq(ATTEMPT_ID), eq(ROLE_ID), any(), any())).thenReturn(1);
        when(generator.generate(eq(context), any())).thenReturn(new DiagnosisGenerator.GenerationResult(
                VALID, "requested", "served", "fake", null, 21L));
        when(diagnostics.completeSuccess(eq(ATTEMPT_ID), eq(ROLE_ID), any(), anyString(), eq("fake"),
                eq("requested"), eq("served"), eq("explainability-v5"), anyString(), anyString(), eq(21L),
                isNull(), isNull(), isNull(), isNull(), eq("QA"), eq("Rol QA"))).thenReturn(1);

        var outcome = service.retryManual(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, outcome.status());
        assertEquals("Actual", outcome.diagnosis().currentSituation());
        verify(diagnostics, never()).claimExpired(any(), anyString(), any(), any(), any());
        verify(generator).generate(eq(context), any());
    }

    @Test
    void retryRejectsSuccessfulDiagnosticsWithoutProviderCall() {
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID))
                .thenReturn(Optional.of(diagnostic(ExplainabilityDiagnosticStatus.SUCCEEDED)));

        ApiProblem error = assertThrows(ApiProblem.class, () -> service.retryManual(USER_ID, ATTEMPT_ID, ROLE_ID));

        assertEquals("DIAGNOSIS_ALREADY_SUCCEEDED", error.code());
        verifyNoInteractions(contexts, generator);
        verify(diagnostics, never()).claimFailed(any(), anyString(), any(), any());
        verify(diagnostics, never()).claimExpired(any(), anyString(), any(), any(), any());
    }

    private ExplainabilityDiagnostic diagnostic(ExplainabilityDiagnosticStatus status) {
        try {
            var constructor = ExplainabilityDiagnostic.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ExplainabilityDiagnostic diagnostic = constructor.newInstance();
            ReflectionTestUtils.setField(diagnostic, "attemptId", ATTEMPT_ID);
            ReflectionTestUtils.setField(diagnostic, "roleId", ROLE_ID);
            ReflectionTestUtils.setField(diagnostic, "status", status);
            ReflectionTestUtils.setField(diagnostic, "attemptCount", 1);
            ReflectionTestUtils.setField(diagnostic, "generationToken", UUID.randomUUID());
            ReflectionTestUtils.setField(diagnostic, "roleNameSnapshot", "QA historical");
            ReflectionTestUtils.setField(diagnostic, "roleDescriptionSnapshot", "Rol histórico");
            if (status == ExplainabilityDiagnosticStatus.SUCCEEDED) {
                ReflectionTestUtils.setField(diagnostic, "diagnosisContent", new ObjectMapper().readTree(VALID));
                ReflectionTestUtils.setField(diagnostic, "modelRequested", "old-model");
                ReflectionTestUtils.setField(diagnostic, "modelServed", "old-served-model");
                ReflectionTestUtils.setField(diagnostic, "promptVersion", "explainability-v5");
                ReflectionTestUtils.setField(diagnostic, "succeededAt", Instant.EPOCH);
            }
            return diagnostic;
        } catch (Exception error) {
            throw new AssertionError(error);
        }
    }
}
