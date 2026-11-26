package com.nextrole.skillgap;

import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nextrole.skillgap.SkillGapV2Dtos.CompetencyGapDto;
import static com.nextrole.skillgap.SkillGapV2Dtos.DiagnosticDto;

@Service
public class SkillGapV2Service {
    private final AssessmentAttemptRepository attempts;
    private final AssessmentAnswerRepository answers;
    private final AssessmentQuestionRepository questions;
    private final AssessmentQuestionnaireVersionRepository questionnaireVersions;
    private final AssessmentMatrixVersionRepository matrixVersions;
    private final AssessmentRoleRequirementRepository requirements;
    private final AssessmentRoleRepository roles;

    public SkillGapV2Service(AssessmentAttemptRepository attempts,
                             AssessmentAnswerRepository answers,
                             AssessmentQuestionRepository questions,
                             AssessmentQuestionnaireVersionRepository questionnaireVersions,
                             AssessmentMatrixVersionRepository matrixVersions,
                             AssessmentRoleRequirementRepository requirements,
                             AssessmentRoleRepository roles) {
        this.attempts = attempts;
        this.answers = answers;
        this.questions = questions;
        this.questionnaireVersions = questionnaireVersions;
        this.matrixVersions = matrixVersions;
        this.requirements = requirements;
        this.roles = roles;
    }

    @Transactional(readOnly = true)
    public DiagnosticDto calculate(UUID userId, UUID attemptId, String roleId) {
        AssessmentAttempt attempt = attempts.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> ApiProblem.notFound("Intento"));
        if (attempt.status != AssessmentAttemptStatus.COMPLETED) {
            throw new ApiProblem(HttpStatus.CONFLICT, "ATTEMPT_NOT_COMPLETED",
                    "Solo se puede consultar una evaluación completada");
        }
        if (!roles.existsById(roleId)) throw ApiProblem.notFound("Rol V2");

        AssessmentQuestionnaireVersion questionnaire = questionnaireVersions.findById(attempt.questionnaireVersionId)
                .orElseThrow(SkillGapV2Service::inconsistentAttempt);
        AssessmentMatrixVersion matrix = matrixVersions.findById(attempt.matrixVersionId)
                .orElseThrow(SkillGapV2Service::inconsistentAttempt);

        Map<String, Integer> declaredLevels = answers.findByIdAttemptId(attempt.id).stream()
                .collect(Collectors.toMap(answer -> answer.id.competencyCode, answer -> (int) answer.level));
        Map<String, AssessmentQuestion> questionByCode = questions
                .findByIdQuestionnaireVersionIdOrderByPosition(attempt.questionnaireVersionId).stream()
                .collect(Collectors.toMap(question -> question.id.competencyCode, Function.identity()));

        List<AssessmentRoleRequirement> relevantRequirements = requirements
                .findByIdMatrixVersionId(attempt.matrixVersionId).stream()
                .filter(requirement -> requirement.id.roleId.equals(roleId))
                .filter(requirement -> requirement.importance != AssessmentRequirementImportance.NOT_RELEVANT)
                .sorted(Comparator.comparingInt(requirement -> {
                    AssessmentQuestion question = questionByCode.get(requirement.id.competencyCode);
                    return question == null ? Integer.MAX_VALUE : question.position;
                }))
                .toList();

        List<CompetencyGapDto> competencies = new ArrayList<>(relevantRequirements.size());
        for (AssessmentRoleRequirement requirement : relevantRequirements) {
            String code = requirement.id.competencyCode;
            AssessmentQuestion question = questionByCode.get(code);
            Integer declared = declaredLevels.get(code);
            if (question == null || declared == null || requirement.expectedLevel == null
                    || requirement.importance == null) {
                throw inconsistentAttempt();
            }

            int required = requirement.expectedLevel.intValue();
            int gap = GapPolicy.amount(declared, required);
            competencies.add(new CompetencyGapDto(code, question.title, declared, required,
                    requirement.importance, gap));
        }

        return new DiagnosticDto(attempt.id, roleId, questionnaire.versionCode, matrix.versionCode,
                List.copyOf(competencies));
    }

    private static ApiProblem inconsistentAttempt() {
        return new ApiProblem(HttpStatus.CONFLICT, "INCONSISTENT_COMPLETED_ATTEMPT",
                "La evaluación completada no contiene los datos necesarios para el diagnóstico");
    }
}
