package com.nextrole.skillgap;

import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nextrole.data.AssessmentRequirementImportance.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillGapV2ServiceTest {
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ATTEMPT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID QUESTIONNAIRE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MATRIX_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final String ROLE_ID = "backend-dev-jr";

    @Mock private AssessmentAttemptRepository attempts;
    @Mock private AssessmentAnswerRepository answers;
    @Mock private AssessmentQuestionRepository questions;
    @Mock private AssessmentQuestionnaireVersionRepository questionnaireVersions;
    @Mock private AssessmentMatrixVersionRepository matrixVersions;
    @Mock private AssessmentRoleRequirementRepository requirements;
    @Mock private AssessmentRoleRepository roles;

    private SkillGapV2Service service;

    @BeforeEach
    void setUp() {
        service = new SkillGapV2Service(attempts, answers, questions, questionnaireVersions,
                matrixVersions, requirements, roles);
    }

    @Test
    void calculatesRelevantGapsIncludingZeroAndPreservesLevelZeroAndVersions() {
        givenCompletedAttempt();
        when(roles.existsById(ROLE_ID)).thenReturn(true);
        when(questionnaireVersions.findById(QUESTIONNAIRE_ID)).thenReturn(Optional.of(
                new AssessmentQuestionnaireVersion(QUESTIONNAIRE_ID, "Q-old", Instant.EPOCH, false)));
        when(matrixVersions.findById(MATRIX_ID)).thenReturn(Optional.of(
                new AssessmentMatrixVersion(MATRIX_ID, "M-old", Instant.EPOCH, false)));
        when(answers.findByIdAttemptId(ATTEMPT_ID)).thenReturn(List.of(
                new AssessmentAnswer(new AssessmentAnswerId(ATTEMPT_ID, "programming"), QUESTIONNAIRE_ID, (short) 0, Instant.EPOCH),
                new AssessmentAnswer(new AssessmentAnswerId(ATTEMPT_ID, "testing"), QUESTIONNAIRE_ID, (short) 4, Instant.EPOCH)));
        when(questions.findByIdQuestionnaireVersionIdOrderByPosition(QUESTIONNAIRE_ID)).thenReturn(List.of(
                question("programming", "Fundamentos", (short) 1),
                question("testing", "Pruebas", (short) 2),
                question("unused", "No requerido", (short) 3)));
        when(requirements.findByIdMatrixVersionId(MATRIX_ID)).thenReturn(List.of(
                requirement("programming", ESSENTIAL, (short) 2),
                requirement("testing", COMPLEMENTARY, (short) 3),
                requirement("unused", NOT_RELEVANT, null)));

        var result = service.calculate(USER_ID, ATTEMPT_ID, ROLE_ID);

        assertEquals("Q-old", result.questionnaireVersion());
        assertEquals("M-old", result.matrixVersion());
        assertEquals(2, result.competencies().size());
        assertEquals("programming", result.competencies().get(0).competencyCode());
        assertEquals("Fundamentos", result.competencies().get(0).competencyName());
        assertEquals(0, result.competencies().get(0).declaredLevel());
        assertEquals(2, result.competencies().get(0).gap());
        assertEquals(0, result.competencies().get(1).gap());
        assertEquals(COMPLEMENTARY, result.competencies().get(1).importance());
        verifyNoMoreInteractions(roles);
    }

    @Test
    void rejectsDraftWithoutReadingItsAnswersOrMatrix() {
        AssessmentAttempt draft = new AssessmentAttempt(ATTEMPT_ID, USER_ID, AssessmentAttemptStatus.DRAFT,
                QUESTIONNAIRE_ID, MATRIX_ID, Instant.EPOCH);
        when(attempts.findByIdAndUserId(ATTEMPT_ID, USER_ID)).thenReturn(Optional.of(draft));

        ApiProblem error = assertThrows(ApiProblem.class, () -> service.calculate(USER_ID, ATTEMPT_ID, ROLE_ID));

        assertEquals("ATTEMPT_NOT_COMPLETED", error.code());
        verifyNoInteractions(answers, questions, requirements, roles);
    }

    @Test
    void doesNotConvertMissingRelevantAnswerToZero() {
        givenCompletedAttempt();
        when(roles.existsById(ROLE_ID)).thenReturn(true);
        when(questionnaireVersions.findById(QUESTIONNAIRE_ID)).thenReturn(Optional.of(
                new AssessmentQuestionnaireVersion(QUESTIONNAIRE_ID, "Q1", Instant.EPOCH, true)));
        when(matrixVersions.findById(MATRIX_ID)).thenReturn(Optional.of(
                new AssessmentMatrixVersion(MATRIX_ID, "M1", Instant.EPOCH, true)));
        when(answers.findByIdAttemptId(ATTEMPT_ID)).thenReturn(List.of());
        when(questions.findByIdQuestionnaireVersionIdOrderByPosition(QUESTIONNAIRE_ID))
                .thenReturn(List.of(question("programming", "Fundamentos", (short) 1)));
        when(requirements.findByIdMatrixVersionId(MATRIX_ID))
                .thenReturn(List.of(requirement("programming", ESSENTIAL, (short) 2)));

        ApiProblem error = assertThrows(ApiProblem.class, () -> service.calculate(USER_ID, ATTEMPT_ID, ROLE_ID));

        assertEquals("INCONSISTENT_COMPLETED_ATTEMPT", error.code());
    }

    @Test
    void returnsEmptyListWhenRoleHasNoRelevantRequirements() {
        givenCompletedAttempt();
        when(roles.existsById(ROLE_ID)).thenReturn(true);
        when(questionnaireVersions.findById(QUESTIONNAIRE_ID)).thenReturn(Optional.of(
                new AssessmentQuestionnaireVersion(QUESTIONNAIRE_ID, "Q1", Instant.EPOCH, true)));
        when(matrixVersions.findById(MATRIX_ID)).thenReturn(Optional.of(
                new AssessmentMatrixVersion(MATRIX_ID, "M1", Instant.EPOCH, true)));
        when(answers.findByIdAttemptId(ATTEMPT_ID)).thenReturn(List.of());
        when(questions.findByIdQuestionnaireVersionIdOrderByPosition(QUESTIONNAIRE_ID)).thenReturn(List.of());
        when(requirements.findByIdMatrixVersionId(MATRIX_ID))
                .thenReturn(List.of(requirement("unrelated", NOT_RELEVANT, null)));

        assertTrue(service.calculate(USER_ID, ATTEMPT_ID, ROLE_ID).competencies().isEmpty());
    }

    private void givenCompletedAttempt() {
        AssessmentAttempt completed = new AssessmentAttempt(ATTEMPT_ID, USER_ID, AssessmentAttemptStatus.COMPLETED,
                QUESTIONNAIRE_ID, MATRIX_ID, Instant.EPOCH);
        completed.completedAt = Instant.EPOCH;
        when(attempts.findByIdAndUserId(ATTEMPT_ID, USER_ID)).thenReturn(Optional.of(completed));
    }

    private static AssessmentRoleRequirement requirement(String code, AssessmentRequirementImportance importance,
                                                         Short expectedLevel) {
        return new AssessmentRoleRequirement(new AssessmentRoleRequirementId(MATRIX_ID, ROLE_ID, code),
                importance, expectedLevel);
    }

    private static AssessmentQuestion question(String code, String title, short position) {
        AssessmentQuestion question = mock(AssessmentQuestion.class);
        question.id = new AssessmentQuestionId(QUESTIONNAIRE_ID, code);
        question.title = title;
        question.position = position;
        return question;
    }
}
