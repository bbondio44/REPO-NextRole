package com.nextrole.assessment;

import com.nextrole.assessment.AssessmentDtos.AttemptDto;
import com.nextrole.assessment.AssessmentDtos.RoleResultDto;
import com.nextrole.assessment.AssessmentDtos.HistoryItemDto;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AssessmentAttemptService {
    private final AssessmentAttemptRepository attempts;
    private final UserAccountRepository users;
    private final AssessmentQuestionnaireVersionRepository questionnaireVersions;
    private final AssessmentMatrixVersionRepository matrixVersions;
    private final AssessmentCatalogService catalog;
    private final AssessmentAnswerService answers;
    private final AssessmentResultSetRepository resultSets;
    private final AssessmentRoleResultRepository roleResults;

    public AssessmentAttemptService(AssessmentAttemptRepository attempts, UserAccountRepository users,
                                    AssessmentQuestionnaireVersionRepository questionnaireVersions,
                                    AssessmentMatrixVersionRepository matrixVersions,
                                    AssessmentCatalogService catalog, AssessmentAnswerService answers,
                                    AssessmentResultSetRepository resultSets,
                                    AssessmentRoleResultRepository roleResults) {
        this.attempts = attempts;
        this.users = users;
        this.questionnaireVersions = questionnaireVersions;
        this.matrixVersions = matrixVersions;
        this.catalog = catalog;
        this.answers = answers;
        this.resultSets = resultSets;
        this.roleResults = roleResults;
    }

    @Transactional
    public StartResult startOrRecover(UUID userId) {
        users.findByIdForUpdate(userId).orElseThrow(() -> ApiProblem.notFound("Usuario"));
        var currentDraft = attempts.findFirstByUserIdAndStatus(userId, AssessmentAttemptStatus.DRAFT);
        if (currentDraft.isPresent()) return new StartResult(currentDraft.get(), false);

        AssessmentQuestionnaireVersion questionnaire = questionnaireVersions.findFirstByCurrentTrue()
                .orElseThrow(() -> ApiProblem.notFound("Cuestionario vigente"));
        AssessmentMatrixVersion matrix = matrixVersions.findFirstByCurrentTrue()
                .orElseThrow(() -> ApiProblem.notFound("Matriz vigente"));

        AssessmentAttempt attempt = new AssessmentAttempt(UUID.randomUUID(), userId, AssessmentAttemptStatus.DRAFT,
                questionnaire.id, matrix.id, Instant.now());
        return new StartResult(attempts.save(attempt), true);
    }

    @Transactional
    public StartResult reevaluate(UUID userId) {
        users.findByIdForUpdate(userId).orElseThrow(() -> ApiProblem.notFound("Usuario"));
        var currentDraft = attempts.findFirstByUserIdAndStatus(userId, AssessmentAttemptStatus.DRAFT);
        if (currentDraft.isPresent()) return new StartResult(currentDraft.get(), false);

        AssessmentQuestionnaireVersion questionnaire = questionnaireVersions.findFirstByCurrentTrue()
                .orElseThrow(() -> ApiProblem.notFound("Cuestionario vigente"));
        AssessmentMatrixVersion matrix = matrixVersions.findFirstByCurrentTrue()
                .orElseThrow(() -> ApiProblem.notFound("Matriz vigente"));
        AssessmentAttempt source = attempts.findFirstByUserIdAndStatusOrderByCompletedAtDesc(
                userId, AssessmentAttemptStatus.COMPLETED).orElse(null);
        AssessmentAttempt target = new AssessmentAttempt(UUID.randomUUID(), userId, AssessmentAttemptStatus.DRAFT,
                questionnaire.id, matrix.id, Instant.now());
        target.sourceAttemptId = source == null ? null : source.id;
        target = attempts.save(target);
        return new StartResult(target, true);
    }

    @Transactional
    public void discard(UUID userId, UUID attemptId) {
        AssessmentAttempt attempt = attempts.findByIdAndUserIdForUpdate(attemptId, userId)
                .orElseThrow(() -> ApiProblem.notFound("Intento"));
        if (attempt.status != AssessmentAttemptStatus.DRAFT) {
            throw ApiProblem.conflict("El intento no admite descarte");
        }
        attempt.status = AssessmentAttemptStatus.DISCARDED;
        attempt.discardedAt = Instant.now();
        attempts.save(attempt);
    }

    @Transactional(readOnly = true)
    public AssessmentAttempt current(UUID userId) {
        return attempts.findFirstByUserIdAndStatus(userId, AssessmentAttemptStatus.DRAFT).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<HistoryItemDto> history(UUID userId) {
        return attempts.findAllByUserIdAndStatusOrderByCompletedAtDesc(userId, AssessmentAttemptStatus.COMPLETED)
                .stream().map(this::toDto)
                .map(attempt -> new HistoryItemDto(attempt.attemptId(), attempt.completedAt(),
                        attempt.questionnaireVersion(), attempt.matrixVersion(), attempt.results()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentAttempt owned(UUID userId, UUID attemptId) {
        return attempts.findByIdAndUserId(attemptId, userId).orElseThrow(() -> ApiProblem.notFound("Intento"));
    }

    @Transactional(readOnly = true)
    public AttemptDto toDto(AssessmentAttempt attempt) {
        AssessmentQuestionnaireVersion questionnaire = questionnaireVersions.findById(attempt.questionnaireVersionId)
                .orElseThrow(() -> ApiProblem.notFound("Versión del cuestionario"));
        AssessmentMatrixVersion matrix = matrixVersions.findById(attempt.matrixVersionId)
                .orElseThrow(() -> ApiProblem.notFound("Versión de la matriz"));
        List<RoleResultDto> storedResults = attempt.status == AssessmentAttemptStatus.COMPLETED
                ? resultSets.findById(attempt.id).map(resultSet -> roleResults
                        .findByIdResultSetIdOrderByRankPosition(attempt.id).stream()
                        .map(result -> new RoleResultDto(result.id.roleId,
                                result.scoreNumerator.toString(), result.scoreDenominator.toString(),
                                result.displayPercentage.toPlainString(), result.rankPosition))
                        .toList()).orElse(List.of())
                : null;

        var questions = catalog.questionsFor(attempt.questionnaireVersionId);
        var answerMap = answers.readAnswers(attempt.id);
        return new AttemptDto(attempt.id, attempt.status, attempt.createdAt, attempt.completedAt,
                questionnaire.versionCode, matrix.versionCode,
                Math.toIntExact(answerMap.size()), questions.size(), answerMap, questions, storedResults);
    }

    public record StartResult(AssessmentAttempt attempt, boolean created) {}
}
