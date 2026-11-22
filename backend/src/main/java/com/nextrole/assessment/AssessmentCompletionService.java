package com.nextrole.assessment;

import com.nextrole.assessment.AssessmentDtos.AttemptDto;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import com.nextrole.matching.MatchingEngineV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentCompletionService {
    private static final int REQUIRED_QUESTIONS = 31;

    private final AssessmentAttemptRepository attempts;
    private final AssessmentQuestionRepository questions;
    private final AssessmentAnswerRepository answers;
    private final AssessmentRoleRepository roles;
    private final AssessmentRoleRequirementRepository requirements;
    private final AssessmentResultSetRepository resultSets;
    private final AssessmentRoleResultRepository roleResults;
    private final AssessmentAttemptService attemptService;
    private final MatchingEngineV2 matching;

    public AssessmentCompletionService(AssessmentAttemptRepository attempts, AssessmentQuestionRepository questions,
                                       AssessmentAnswerRepository answers, AssessmentRoleRepository roles,
                                       AssessmentRoleRequirementRepository requirements,
                                       AssessmentResultSetRepository resultSets,
                                       AssessmentRoleResultRepository roleResults,
                                       AssessmentAttemptService attemptService, MatchingEngineV2 matching) {
        this.attempts = attempts;
        this.questions = questions;
        this.answers = answers;
        this.roles = roles;
        this.requirements = requirements;
        this.resultSets = resultSets;
        this.roleResults = roleResults;
        this.attemptService = attemptService;
        this.matching = matching;
    }

    @Transactional
    public AttemptDto complete(UUID userId, UUID attemptId) {
        AssessmentAttempt attempt = attempts.findByIdAndUserIdForUpdate(attemptId, userId)
                .orElseThrow(() -> ApiProblem.notFound("Intento"));
        if (attempt.status == AssessmentAttemptStatus.COMPLETED) return attemptService.toDto(attempt);
        if (attempt.status != AssessmentAttemptStatus.DRAFT) {
            throw ApiProblem.conflict("El intento no admite finalización");
        }

        List<AssessmentQuestion> versionQuestions = questions
                .findByIdQuestionnaireVersionIdOrderByPosition(attempt.questionnaireVersionId);
        if (versionQuestions.size() != REQUIRED_QUESTIONS) {
            throw new IllegalStateException("La versión del cuestionario no contiene 31 competencias");
        }
        List<AssessmentAnswer> savedAnswers = answers.findByIdAttemptIdOrderByIdCompetencyCode(attempt.id);
        Map<String, Integer> answerLevels = savedAnswers.stream().collect(Collectors.toMap(
                answer -> answer.id.competencyCode, answer -> (int) answer.level));
        List<String> missingCompetencies = versionQuestions.stream()
                .map(question -> question.id.competencyCode)
                .filter(code -> !answerLevels.containsKey(code))
                .toList();
        if (!missingCompetencies.isEmpty() || answerLevels.size() != REQUIRED_QUESTIONS) {
            throw ApiProblem.incompleteAssessment(missingCompetencies);
        }

        List<String> roleIds = roles.findAll().stream().map(role -> role.id).sorted().toList();
        List<AssessmentRoleRequirement> matrixRows = requirements.findByIdMatrixVersionId(attempt.matrixVersionId);
        List<MatchingEngineV2.RoleScore> scores = matching.calculate(roleIds, matrixRows, answerLevels);
        if (scores.size() != 6) throw new IllegalStateException("El motor V2 no produjo los seis resultados esperados");

        Instant calculatedAt = Instant.now();
        resultSets.save(new AssessmentResultSet(attempt.id, MatchingEngineV2.ALGORITHM_VERSION, calculatedAt));
        List<AssessmentRoleResult> persisted = new ArrayList<>(6);
        for (MatchingEngineV2.RoleScore score : scores) {
            persisted.add(new AssessmentRoleResult(
                    new AssessmentRoleResultId(attempt.id, score.roleId()),
                    score.scoreNumerator(), score.scoreDenominator(), new BigDecimal(score.displayPercentage()),
                    (short) score.rankPosition()));
        }
        roleResults.saveAll(persisted);
        attempt.status = AssessmentAttemptStatus.COMPLETED;
        attempt.completedAt = calculatedAt;
        attempts.save(attempt);
        return attemptService.toDto(attempt);
    }
}
