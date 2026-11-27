package com.nextrole.explainability;

import com.nextrole.assessment.AssessmentAttemptService;
import com.nextrole.assessment.AssessmentDtos.AttemptDto;
import com.nextrole.assessment.AssessmentDtos.QuestionDto;
import com.nextrole.assessment.AssessmentDtos.RoleResultDto;
import com.nextrole.assessment.RoleCatalogService;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.AssessmentAttempt;
import com.nextrole.data.AssessmentAttemptStatus;
import com.nextrole.data.AssessmentRequirementImportance;
import com.nextrole.skillgap.SkillGapV2Dtos.CompetencyGapDto;
import com.nextrole.skillgap.SkillGapV2Dtos.DiagnosticDto;
import com.nextrole.skillgap.SkillGapV2Service;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExplainabilityContextService {
    private final AssessmentAttemptService attempts;
    private final RoleCatalogService roles;
    private final SkillGapV2Service skillGaps;

    public ExplainabilityContextService(AssessmentAttemptService attempts, RoleCatalogService roles,
                                        SkillGapV2Service skillGaps) {
        this.attempts = attempts;
        this.roles = roles;
        this.skillGaps = skillGaps;
    }

    public ExplainabilityContext build(UUID userId, UUID attemptId, String roleId) {
        AssessmentAttempt attempt = attempts.owned(userId, attemptId);
        if (attempt.status != AssessmentAttemptStatus.COMPLETED) {
            throw new ApiProblem(HttpStatus.CONFLICT, "ATTEMPT_NOT_COMPLETED",
                    "Solo se puede generar un diagnóstico para una evaluación completada");
        }

        AttemptDto attemptData = attempts.toDto(attempt);
        RoleCatalogService.RoleDescription role = roles.one(roleId);
        DiagnosticDto gaps = skillGaps.calculate(userId, attemptId, roleId);
        if (!attemptData.attemptId().equals(attemptId)
                || !gaps.attemptId().equals(attemptId)
                || !gaps.roleId().equals(roleId)
                || !gaps.questionnaireVersion().equals(attemptData.questionnaireVersion())
                || !gaps.matrixVersion().equals(attemptData.matrixVersion())) {
            throw inconsistentAttempt();
        }

        RoleResultDto matching = attemptData.results() == null ? null : attemptData.results().stream()
                .filter(result -> roleId.equals(result.roleId())).findFirst().orElse(null);
        if (matching == null) throw inconsistentAttempt();
        BigDecimal percentage;
        try {
            percentage = new BigDecimal(matching.displayPercentage());
        } catch (NumberFormatException e) {
            throw inconsistentAttempt();
        }

        Map<String, QuestionDto> questions = attemptData.questions().stream()
                .collect(Collectors.toMap(QuestionDto::competencyCode, Function.identity()));
        Map<String, Integer> answers = attemptData.answers();
        var competencies = gaps.competencies().stream().map(gap -> {
            if (gap.importance() == AssessmentRequirementImportance.NOT_RELEVANT) throw inconsistentAttempt();
            QuestionDto question = questions.get(gap.competencyCode());
            Integer answerLevel = answers.get(gap.competencyCode());
            if (question == null || answerLevel == null || answerLevel != gap.declaredLevel()) throw inconsistentAttempt();
            var currentOption = question.options().stream().filter(option -> option.level() == gap.declaredLevel())
                    .findFirst().orElseThrow(ExplainabilityContextService::inconsistentAttempt);
            var requiredOption = question.options().stream().filter(option -> option.level() == gap.requiredLevel())
                    .findFirst().orElseThrow(ExplainabilityContextService::inconsistentAttempt);
            int levelGap = Math.max(0, gap.requiredLevel() - gap.declaredLevel());
            if (levelGap != gap.gap()) throw inconsistentAttempt();
            var classification = ExplainabilityCompetencyContext.Classification.fromImportance(gap.importance());
            int priority = levelGap * classification.weight();
            return new ExplainabilityCompetencyContext(gap.competencyCode(), gap.competencyName(),
                    new ExplainabilityCompetencyContext.LevelContext(gap.declaredLevel(), currentOption.description()),
                    new ExplainabilityCompetencyContext.LevelContext(gap.requiredLevel(), requiredOption.description()),
                    classification, gap.gap(), priority);
        }).toList();

        try {
            return new ExplainabilityContext(role.roleId(), role.name(), role.description(), percentage, competencies);
        } catch (IllegalArgumentException e) {
            throw inconsistentAttempt();
        }
    }

    private static ApiProblem inconsistentAttempt() {
        return new ApiProblem(HttpStatus.CONFLICT, "INCONSISTENT_COMPLETED_ATTEMPT",
                "La evaluación completada no contiene los datos necesarios para el diagnóstico");
    }
}
