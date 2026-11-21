package com.nextrole.assessment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextrole.data.AssessmentAttemptStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AssessmentDtos {
    private AssessmentDtos() {}

    public record AnswerOptionDto(int level, String description) {}

    public record QuestionDto(String competencyCode, int position, String title, String question,
                              List<AnswerOptionDto> options) {}

    public record CatalogDto(String questionnaireVersion, List<QuestionDto> questions) {}

    public record RoleResultDto(String roleId, String scoreNumerator, String scoreDenominator,
                                String displayPercentage, int rankPosition) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AttemptDto(UUID attemptId, AssessmentAttemptStatus status, Instant createdAt,
                             Instant completedAt, String questionnaireVersion, String matrixVersion,
                             int answeredCount, int totalQuestions, Map<String, Integer> answers,
                             List<QuestionDto> questions, List<RoleResultDto> results) {}

    public record AnswerSavedDto(String competencyCode, int level, int answeredCount, int totalQuestions) {}

    public record SaveAnswerRequest(@NotNull @Min(0) @Max(4) Integer level) {}
}
