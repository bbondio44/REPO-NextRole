package com.nextrole.assessment;

import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentAttemptServiceTest {
    private static final UUID USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID QUESTIONNAIRE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MATRIX_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ATTEMPT_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Mock AssessmentAttemptRepository attempts;
    @Mock UserAccountRepository users;
    @Mock AssessmentQuestionnaireVersionRepository questionnaireVersions;
    @Mock AssessmentMatrixVersionRepository matrixVersions;
    @Mock AssessmentCatalogService catalog;
    @Mock AssessmentAnswerService answerService;
    @Mock AssessmentAnswerRepository answerRepository;
    @Mock AssessmentResultSetRepository resultSets;
    @Mock AssessmentRoleResultRepository roleResults;

    @Test
    void startPinsCurrentVersionsAndCreatesOnlyOneDraft() {
        var service = service();
        when(users.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user()));
        AssessmentAttempt persistedDraft = draft(ATTEMPT_ID, USER_ID, QUESTIONNAIRE_ID, MATRIX_ID);
        when(attempts.findFirstByUserIdAndStatus(USER_ID, AssessmentAttemptStatus.DRAFT))
                .thenReturn(Optional.empty(), Optional.of(persistedDraft));
        when(questionnaireVersions.findFirstByCurrentTrue()).thenReturn(Optional.of(questionnaireVersion(QUESTIONNAIRE_ID, "Q1")));
        when(matrixVersions.findFirstByCurrentTrue()).thenReturn(Optional.of(matrixVersion(MATRIX_ID, "M1")));
        when(attempts.save(any(AssessmentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.startOrRecover(USER_ID);
        var recovered = service.startOrRecover(USER_ID);

        assertTrue(created.created());
        assertEquals(QUESTIONNAIRE_ID, created.attempt().questionnaireVersionId);
        assertEquals(MATRIX_ID, created.attempt().matrixVersionId);
        assertEquals(AssessmentAttemptStatus.DRAFT, created.attempt().status);
        assertFalse(recovered.created());
        verify(attempts, times(2)).findFirstByUserIdAndStatus(USER_ID, AssessmentAttemptStatus.DRAFT);
        verify(attempts, times(1)).save(any(AssessmentAttempt.class));
        verify(users, times(2)).findByIdForUpdate(USER_ID);
    }

    @Test
    void recoveryKeepsVersionsPinnedOnExistingDraft() {
        var service = service();
        AssessmentAttempt existing = draft(ATTEMPT_ID, USER_ID, QUESTIONNAIRE_ID, MATRIX_ID);
        when(users.findByIdForUpdate(USER_ID)).thenReturn(Optional.of(user()));
        when(attempts.findFirstByUserIdAndStatus(USER_ID, AssessmentAttemptStatus.DRAFT)).thenReturn(Optional.of(existing));

        var result = service.startOrRecover(USER_ID);

        assertFalse(result.created());
        assertSame(existing, result.attempt());
        verify(questionnaireVersions, never()).findFirstByCurrentTrue();
        verify(matrixVersions, never()).findFirstByCurrentTrue();
        verify(attempts, never()).save(any());
    }

    @Test
    void answerLevelZeroIsStoredAndRemainsDistinctFromAnAbsentAnswer() {
        var service = answerService();
        AssessmentAttempt attempt = draft(ATTEMPT_ID, USER_ID, QUESTIONNAIRE_ID, MATRIX_ID);
        AssessmentAnswerId answerId = new AssessmentAnswerId(ATTEMPT_ID, "programming-fundamentals");
        when(attempts.findByIdAndUserId(ATTEMPT_ID, USER_ID)).thenReturn(Optional.of(attempt));
        when(catalog.containsQuestion(QUESTIONNAIRE_ID, "programming-fundamentals")).thenReturn(true);
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());
        when(answerRepository.save(any(AssessmentAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(answerRepository.countByIdAttemptId(ATTEMPT_ID)).thenReturn(1L);
        when(answerRepository.findByIdAttemptIdOrderByIdCompetencyCode(ATTEMPT_ID))
                .thenReturn(java.util.List.of(savedAnswer(answerId, (short) 0)));

        var saved = service.save(USER_ID, ATTEMPT_ID, "programming-fundamentals", 0);
        var answerMap = service.readAnswers(ATTEMPT_ID);

        assertEquals(0, saved.level());
        assertEquals(1, saved.answeredCount());
        assertEquals(31, saved.totalQuestions());
        assertEquals(0, answerMap.get("programming-fundamentals"));
        assertFalse(answerMap.containsKey("algorithms"));
    }

    @Test
    void draftAnswerCanBeEditedButCompletedAttemptIsImmutable() {
        var service = answerService();
        AssessmentAttempt attempt = draft(ATTEMPT_ID, USER_ID, QUESTIONNAIRE_ID, MATRIX_ID);
        AssessmentAnswerId answerId = new AssessmentAnswerId(ATTEMPT_ID, "algorithms");
        AssessmentAnswer existing = savedAnswer(answerId, (short) 1);
        when(attempts.findByIdAndUserId(ATTEMPT_ID, USER_ID)).thenReturn(Optional.of(attempt));
        when(catalog.containsQuestion(QUESTIONNAIRE_ID, "algorithms")).thenReturn(true);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(existing));
        when(answerRepository.save(any(AssessmentAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(answerRepository.countByIdAttemptId(ATTEMPT_ID)).thenReturn(1L);

        var updated = service.save(USER_ID, ATTEMPT_ID, "algorithms", 3);
        assertEquals(3, updated.level());
        assertEquals(3, existing.level);

        attempt.status = AssessmentAttemptStatus.COMPLETED;
        ApiProblem problem = assertThrows(ApiProblem.class, () -> service.save(USER_ID, ATTEMPT_ID, "algorithms", 4));
        assertEquals(HttpStatus.CONFLICT, problem.status());
        assertEquals(3, existing.level);
        verify(answerRepository, times(1)).save(any(AssessmentAnswer.class));
    }

    private AssessmentAttemptService service() {
        return new AssessmentAttemptService(attempts, users, questionnaireVersions, matrixVersions,
                catalog, answerService, resultSets, roleResults);
    }

    private AssessmentAnswerService answerService() {
        return new AssessmentAnswerService(attempts, catalog, answerRepository);
    }

    private static UserAccount user() {
        return new UserAccount(USER_ID, "student@example.test", "Ada", "Prueba", "hash", "STUDENT", Instant.now());
    }

    private static AssessmentQuestionnaireVersion questionnaireVersion(UUID id, String code) {
        return new AssessmentQuestionnaireVersion(id, code, Instant.now(), true);
    }

    private static AssessmentMatrixVersion matrixVersion(UUID id, String code) {
        return new AssessmentMatrixVersion(id, code, Instant.now(), true);
    }

    private static AssessmentAttempt draft(UUID id, UUID userId, UUID questionnaireId, UUID matrixId) {
        return new AssessmentAttempt(id, userId, AssessmentAttemptStatus.DRAFT, questionnaireId, matrixId, Instant.now());
    }

    private static AssessmentAnswer savedAnswer(AssessmentAnswerId id, short level) {
        return new AssessmentAnswer(id, QUESTIONNAIRE_ID, level, Instant.now());
    }
}
