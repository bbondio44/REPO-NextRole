package com.nextrole.explainability;

import com.nextrole.assessment.AssessmentAttemptService;
import com.nextrole.assessment.AssessmentDtos;
import com.nextrole.assessment.RoleCatalogService;
import com.nextrole.data.AssessmentAttempt;
import com.nextrole.data.AssessmentAttemptStatus;
import com.nextrole.data.AssessmentRequirementImportance;
import com.nextrole.skillgap.SkillGapV2Dtos;
import com.nextrole.skillgap.SkillGapV2Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExplainabilityContextServiceTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ATTEMPT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID QUESTIONNAIRE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MATRIX_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final String ROLE_ID = "qa-jr";

    @Mock private AssessmentAttemptService attempts;
    @Mock private RoleCatalogService roles;
    @Mock private SkillGapV2Service skillGaps;
    private ExplainabilityContextService service;

    @BeforeEach
    void setUp() {
        service = new ExplainabilityContextService(attempts, roles, skillGaps);
    }

    @Test
    void projectsOnlyRoleCompetenciesUsesPinnedDescriptionsAndPersistedMatching() {
        var attempt = completedAttempt();
        when(attempts.owned(USER_ID, ATTEMPT_ID)).thenReturn(attempt);
        when(attempts.toDto(attempt)).thenReturn(attemptDto(ATTEMPT_ID, "Q1", "M1", "73.4",
                List.of(question("api-testing", 1, "Pruebas de APIs", 0, "No comprendo pruebas", 2, "Puedo probar endpoints"),
                        question("fundamentals", 2, "Fundamentos", 3, "Uso funciones", 2, "Comparo soluciones")),
                Map.of("api-testing", 0, "fundamentals", 3)));
        when(roles.one(ROLE_ID)).thenReturn(new RoleCatalogService.RoleDescription(
                ROLE_ID, "QA", "Descripción profesional del rol"));
        when(skillGaps.calculate(USER_ID, ATTEMPT_ID, ROLE_ID)).thenReturn(new SkillGapV2Dtos.DiagnosticDto(
                ATTEMPT_ID, ROLE_ID, "Q1", "M1", List.of(
                new SkillGapV2Dtos.CompetencyGapDto("api-testing", "Pruebas de APIs", 0, 2,
                        AssessmentRequirementImportance.ESSENTIAL, 2),
                new SkillGapV2Dtos.CompetencyGapDto("fundamentals", "Fundamentos", 3, 2,
                        AssessmentRequirementImportance.COMPLEMENTARY, 0))));

        ExplainabilityContext context = service.build(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals(ROLE_ID, context.roleId());
        assertEquals("QA", context.roleName());
        assertEquals("Descripción profesional del rol", context.roleDescription());
        assertEquals("73.4", context.matchingPercentage().toPlainString());
        assertEquals(2, context.competencies().size());
        assertEquals("No comprendo pruebas", context.competencies().get(0).current().description());
        assertEquals("Puedo probar endpoints", context.competencies().get(0).required().description());
        assertEquals(2, context.competencies().get(0).gap());
        assertEquals(4, context.competencies().get(0).trainingPriority());
        assertEquals(ExplainabilityCompetencyContext.Classification.ESSENTIAL,
                context.competencies().get(0).classification());
        assertEquals(0, context.competencies().get(1).gap());
        assertEquals(0, context.competencies().get(1).trainingPriority());
        assertEquals(ExplainabilityCompetencyContext.Classification.COMPLEMENTARY,
                context.competencies().get(1).classification());
        verify(skillGaps).calculate(USER_ID, ATTEMPT_ID, ROLE_ID);
    }

    @Test
    void preservesHistoricalQuestionnaireDescriptionsAndCoveredProfileWithoutInventingGaps() {
        UUID historicalAttemptId = UUID.fromString("00000000-0000-0000-0000-000000000022");
        var attempt = completedAttempt(historicalAttemptId);
        when(attempts.owned(USER_ID, historicalAttemptId)).thenReturn(attempt);
        when(attempts.toDto(attempt)).thenReturn(attemptDto(historicalAttemptId, "Q2", "M1", "100.0",
                List.of(question("api-testing", 1, "Pruebas de APIs", 4, "Q2 actual descriptor", 2, "Q2 requerido descriptor")),
                Map.of("api-testing", 4)));
        when(roles.one(ROLE_ID)).thenReturn(new RoleCatalogService.RoleDescription(
                ROLE_ID, "QA", "Descripción del catálogo"));
        when(skillGaps.calculate(USER_ID, historicalAttemptId, ROLE_ID)).thenReturn(new SkillGapV2Dtos.DiagnosticDto(
                historicalAttemptId, ROLE_ID, "Q2", "M1", List.of(new SkillGapV2Dtos.CompetencyGapDto(
                "api-testing", "Pruebas de APIs", 4, 2, AssessmentRequirementImportance.ESSENTIAL, 0))));

        ExplainabilityContext context = service.build(USER_ID, historicalAttemptId, ROLE_ID);

        assertEquals("Q2 actual descriptor", context.competencies().getFirst().current().description());
        assertEquals("Q2 requerido descriptor", context.competencies().getFirst().required().description());
        assertEquals(0, context.competencies().getFirst().gap());
        assertEquals(0, context.competencies().getFirst().trainingPriority());
        verify(attempts).owned(USER_ID, historicalAttemptId);
        verify(skillGaps).calculate(USER_ID, historicalAttemptId, ROLE_ID);
    }

    private static AssessmentAttempt completedAttempt() { return completedAttempt(ATTEMPT_ID); }

    private static AssessmentAttempt completedAttempt(UUID id) {
        AssessmentAttempt attempt = new AssessmentAttempt(id, USER_ID, AssessmentAttemptStatus.COMPLETED,
                QUESTIONNAIRE_ID, MATRIX_ID, Instant.EPOCH);
        attempt.completedAt = Instant.EPOCH;
        return attempt;
    }

    private static AssessmentDtos.AttemptDto attemptDto(UUID attemptId, String questionnaire, String matrix,
                                                          String percentage,
                                                          List<AssessmentDtos.QuestionDto> questions,
                                                          Map<String, Integer> answers) {
        return new AssessmentDtos.AttemptDto(attemptId, AssessmentAttemptStatus.COMPLETED, Instant.EPOCH,
                Instant.EPOCH, questionnaire, matrix, answers.size(), questions.size(), answers, questions,
                List.of(new AssessmentDtos.RoleResultDto(ROLE_ID, "734", "1000", percentage, 1)));
    }

    private static AssessmentDtos.QuestionDto question(String code, int position, String title,
                                                         int currentLevel, String currentDescription,
                                                         int requiredLevel, String requiredDescription) {
        return new AssessmentDtos.QuestionDto(code, position, title, "¿Pregunta?", List.of(
                new AssessmentDtos.AnswerOptionDto(currentLevel, currentDescription),
                new AssessmentDtos.AnswerOptionDto(requiredLevel, requiredDescription)));
    }
}
