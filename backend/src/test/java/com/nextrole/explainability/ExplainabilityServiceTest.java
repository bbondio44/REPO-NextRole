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
import org.mockito.ArgumentCaptor;
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
class ExplainabilityServiceTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ATTEMPT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String ROLE_ID = "qa-jr";
    private static final String DIAGNOSIS_JSON = "{\"currentSituation\":\"Actual\",\"recommendedFocus\":\"Focus\","
            + "\"importanceExplanation\":\"Why\",\"professionalRelevance\":\"Work\","
            + "\"highlightedCompetencyIds\":[\"api-testing\"]}";

    @Mock private AssessmentAttemptService attempts;
    @Mock private RoleCatalogService roles;
    @Mock private ExplainabilityDiagnosticRepository diagnostics;
    @Mock private ExplainabilityContextService contexts;
    @Mock private DiagnosisGenerator generator;
    private ExplainabilityProperties properties;
    private ExplainabilityService service;
    private ExplainabilityContext context;

    @BeforeEach
    void setUp() {
        properties = new ExplainabilityProperties();
        context = new ExplainabilityContext(ROLE_ID, "QA", "Descripción guardada", BigDecimal.valueOf(70),
                List.of(new ExplainabilityCompetencyContext("api-testing", "Pruebas de APIs",
                        new ExplainabilityCompetencyContext.LevelContext(1, "Comprendo pruebas"),
                        new ExplainabilityCompetencyContext.LevelContext(2, "Puedo probar"),
                        ExplainabilityCompetencyContext.Classification.ESSENTIAL, 1, 2)));
        service = new ExplainabilityService(attempts, roles, diagnostics, contexts, generator,
                new ExplainabilityPromptCatalog(), new DiagnosisValidator(new ObjectMapper()), properties,
                new ObjectMapper());
        lenient().when(roles.one(ROLE_ID)).thenReturn(new RoleCatalogService.RoleDescription(ROLE_ID, "QA", "Descripción guardada"));
    }

    @Test
    void createsOneInitialClaimValidatesPersistsSnapshotsAndReturnsTheDiagnosis() throws Exception {
        givenOwnedCompletedAttempt();
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.empty());
        when(contexts.build(USER_ID, ATTEMPT_ID, ROLE_ID)).thenReturn(context);
        when(diagnostics.createGenerating(any(), eq(ATTEMPT_ID), eq(ROLE_ID), any(), any(), eq("QA"), eq("Descripción guardada")))
                .thenReturn(1);
        when(generator.generate(eq(context), any())).thenReturn(new DiagnosisGenerator.GenerationResult(
                DIAGNOSIS_JSON, properties.getModel(), "served/model", "fake",
                new DiagnosisGenerator.Usage(100L, 50L, null, null), 25L));
        when(diagnostics.completeSuccess(eq(ATTEMPT_ID), eq(ROLE_ID), any(), anyString(), eq("fake"),
                eq(properties.getModel()), eq("served/model"), eq("explainability-v5"), anyString(), anyString(),
                eq(25L), eq(100L), eq(50L), isNull(), isNull(), eq("QA"), eq("Descripción guardada"))).thenReturn(1);

        var result = service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, result.status());
        assertEquals("Actual", result.diagnosis().currentSituation());
        assertEquals("Descripción guardada", result.roleDescription());
        verify(generator, times(1)).generate(eq(context), any());
        ArgumentCaptor<String> serialized = ArgumentCaptor.forClass(String.class);
        verify(diagnostics).completeSuccess(eq(ATTEMPT_ID), eq(ROLE_ID), any(), serialized.capture(), anyString(),
                anyString(), any(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any(),
                anyString(), anyString());
        assertEquals(DIAGNOSIS_JSON, serialized.getValue());
    }

    @Test
    void returnsAnExistingSuccessWithoutCallingContextOrProvider() {
        givenOwnedCompletedAttempt();
        ExplainabilityDiagnostic saved = diagnostic(ExplainabilityDiagnosticStatus.SUCCEEDED);
        saved.diagnosisContent = new ObjectMapper().valueToTree(new DiagnosisValidator.ValidatedDiagnosis(
                "Saved", "Saved focus", "Saved why", "Saved relevance", List.of("api-testing")));
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.of(saved));

        var result = service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, result.status());
        assertEquals("Saved", result.diagnosis().currentSituation());
        assertEquals("Historical description", result.roleDescription());
        verifyNoInteractions(contexts, generator);
        verify(diagnostics, never()).createGenerating(any(), any(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void reportsAnInProgressOrFailedRowWithoutCallingTheProvider() {
        givenOwnedCompletedAttempt();
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID))
                .thenReturn(Optional.of(diagnostic(ExplainabilityDiagnosticStatus.GENERATING)))
                .thenReturn(Optional.of(diagnostic(ExplainabilityDiagnosticStatus.FAILED)));

        var generating = service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID);
        ApiProblem failed = assertThrows(ApiProblem.class, () -> service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID));

        assertEquals(ExplainabilityDiagnosticStatus.GENERATING, generating.status());
        assertEquals("DIAGNOSIS_RETRY_REQUIRED", failed.code());
        verifyNoInteractions(contexts, generator);
        verify(diagnostics, never()).createGenerating(any(), any(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void aConcurrentInitialClaimReturnsTheWinnerStateWithoutCallingTheProvider() {
        givenOwnedCompletedAttempt();
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID))
                .thenReturn(Optional.empty(), Optional.of(diagnostic(ExplainabilityDiagnosticStatus.GENERATING)));
        when(contexts.build(USER_ID, ATTEMPT_ID, ROLE_ID)).thenReturn(context);
        when(diagnostics.createGenerating(any(), eq(ATTEMPT_ID), eq(ROLE_ID), any(), any(), eq("QA"), eq("Descripción guardada")))
                .thenReturn(0);

        var result = service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.GENERATING, result.status());
        verifyNoInteractions(generator);
        verify(diagnostics, never()).completeSuccess(any(), anyString(), any(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString(), anyString());
    }

    @Test
    void refusesUnownedOrIncompleteAttemptsBeforeReadingDiagnostics() {
        when(attempts.owned(USER_ID, ATTEMPT_ID)).thenReturn(new AssessmentAttempt(ATTEMPT_ID, USER_ID,
                AssessmentAttemptStatus.DRAFT, UUID.randomUUID(), UUID.randomUUID(), Instant.EPOCH));

        ApiProblem error = assertThrows(ApiProblem.class, () -> service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID));

        assertEquals("ATTEMPT_NOT_COMPLETED", error.code());
        verifyNoInteractions(diagnostics, contexts, generator);
    }

    @Test
    void storesAProviderFailureAsFailedAndDoesNotPersistDiagnosisContent() {
        givenOwnedCompletedAttempt();
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.empty());
        when(contexts.build(USER_ID, ATTEMPT_ID, ROLE_ID)).thenReturn(context);
        when(diagnostics.createGenerating(any(), eq(ATTEMPT_ID), eq(ROLE_ID), any(), any(), anyString(), anyString()))
                .thenReturn(1);
        when(generator.generate(eq(context), any())).thenThrow(new DiagnosisGenerator.GenerationException(
                DiagnosisGenerator.FailureCode.PROVIDER_UNAVAILABLE));
        when(diagnostics.completeFailure(eq(ATTEMPT_ID), eq(ROLE_ID), any(), eq("PROVIDER_UNAVAILABLE"))).thenReturn(1);

        var result = service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.FAILED, result.status());
        assertEquals("PROVIDER_UNAVAILABLE", result.failureCode());
        assertNull(result.diagnosis());
        verify(diagnostics, never()).completeSuccess(any(), anyString(), any(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString(), anyString());
    }

    @Test
    void invalidGeneratedJsonFailsTheClaimAndNeverPersistsSuccess() {
        givenOwnedCompletedAttempt();
        when(diagnostics.findByAttemptIdAndRoleId(ATTEMPT_ID, ROLE_ID)).thenReturn(Optional.empty());
        when(contexts.build(USER_ID, ATTEMPT_ID, ROLE_ID)).thenReturn(context);
        when(diagnostics.createGenerating(any(), eq(ATTEMPT_ID), eq(ROLE_ID), any(), any(), anyString(), anyString()))
                .thenReturn(1);
        when(generator.generate(eq(context), any())).thenReturn(new DiagnosisGenerator.GenerationResult(
                "{\"currentSituation\":\"x\",\"recommendedFocus\":\"y\",\"importanceExplanation\":\"z\",\"professionalRelevance\":\"w\",\"highlightedCompetencyIds\":[\"not-in-context\"]}",
                properties.getModel(), null, "fake", null, null));
        when(diagnostics.completeFailure(eq(ATTEMPT_ID), eq(ROLE_ID), any(),
                eq("INVALID_PROVIDER_RESPONSE"))).thenReturn(1);

        var result = service.generateInitial(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.FAILED, result.status());
        assertEquals("INVALID_PROVIDER_RESPONSE", result.failureCode());
        assertNull(result.diagnosis());
        verify(diagnostics, never()).completeSuccess(any(), anyString(), any(), anyString(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString(), anyString());
    }

    private void givenOwnedCompletedAttempt() {
        when(attempts.owned(USER_ID, ATTEMPT_ID)).thenReturn(new AssessmentAttempt(ATTEMPT_ID, USER_ID,
                AssessmentAttemptStatus.COMPLETED, UUID.randomUUID(), UUID.randomUUID(), Instant.EPOCH));
    }

    private ExplainabilityDiagnostic diagnostic(ExplainabilityDiagnosticStatus status) {
        ExplainabilityDiagnostic diagnostic;
        try {
            var constructor = ExplainabilityDiagnostic.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            diagnostic = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        ReflectionTestUtils.setField(diagnostic, "attemptId", ATTEMPT_ID);
        ReflectionTestUtils.setField(diagnostic, "roleId", ROLE_ID);
        ReflectionTestUtils.setField(diagnostic, "status", status);
        ReflectionTestUtils.setField(diagnostic, "failureCode",
                status == ExplainabilityDiagnosticStatus.FAILED ? "PROVIDER_ERROR" : null);
        ReflectionTestUtils.setField(diagnostic, "leaseUntil",
                status == ExplainabilityDiagnosticStatus.GENERATING ? Instant.now().plusSeconds(30) : null);
        ReflectionTestUtils.setField(diagnostic, "roleNameSnapshot", "QA");
        ReflectionTestUtils.setField(diagnostic, "roleDescriptionSnapshot", "Historical description");
        return diagnostic;
    }
}
