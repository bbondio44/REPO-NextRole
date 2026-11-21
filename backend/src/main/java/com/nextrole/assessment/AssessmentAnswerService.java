package com.nextrole.assessment;

import com.nextrole.assessment.AssessmentDtos.AnswerSavedDto;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AssessmentAnswerService {
    private static final int TOTAL_QUESTIONS = 31;

    private final AssessmentAttemptRepository attempts;
    private final AssessmentCatalogService catalog;
    private final AssessmentAnswerRepository answers;

    public AssessmentAnswerService(AssessmentAttemptRepository attempts, AssessmentCatalogService catalog,
                                  AssessmentAnswerRepository answers) {
        this.attempts = attempts;
        this.catalog = catalog;
        this.answers = answers;
    }

    @Transactional
    public AnswerSavedDto save(UUID userId, UUID attemptId, String competencyCode, Integer level) {
        if (level == null || level < 0 || level > 4) {
            throw ApiProblem.invalid("El nivel debe estar entre 0 y 4");
        }

        AssessmentAttempt attempt = attempts.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> ApiProblem.notFound("Intento"));
        if (attempt.status != AssessmentAttemptStatus.DRAFT) {
            throw ApiProblem.conflict("El intento no admite cambios");
        }
        if (!catalog.containsQuestion(attempt.questionnaireVersionId, competencyCode)) {
            throw ApiProblem.invalid("La competencia no pertenece a la versión del cuestionario del intento");
        }

        AssessmentAnswerId answerId = new AssessmentAnswerId(attemptId, competencyCode);
        Instant updatedAt = Instant.now();
        AssessmentAnswer answer = answers.findById(answerId).orElse(null);
        if (answer == null) {
            answer = new AssessmentAnswer(answerId, attempt.questionnaireVersionId, level.shortValue(), updatedAt);
        } else {
            answer.questionnaireVersionId = attempt.questionnaireVersionId;
            answer.level = level.shortValue();
            answer.updatedAt = updatedAt;
        }
        answers.save(answer);

        int answeredCount = Math.toIntExact(answers.countByIdAttemptId(attemptId));
        return new AnswerSavedDto(competencyCode, level, answeredCount, TOTAL_QUESTIONS);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> readAnswers(UUID attemptId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        answers.findByIdAttemptIdOrderByIdCompetencyCode(attemptId)
                .forEach(answer -> result.put(answer.id.competencyCode, (int) answer.level));
        return result;
    }
}
