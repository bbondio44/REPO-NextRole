package com.nextrole.assessment;

import com.nextrole.assessment.AssessmentDtos.AnswerOptionDto;
import com.nextrole.assessment.AssessmentDtos.CatalogDto;
import com.nextrole.assessment.AssessmentDtos.QuestionDto;
import com.nextrole.common.ApiProblem;
import com.nextrole.data.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentCatalogService {
    private final AssessmentQuestionnaireVersionRepository questionnaireVersions;
    private final AssessmentQuestionRepository questions;
    private final AssessmentAnswerOptionRepository options;

    public AssessmentCatalogService(AssessmentQuestionnaireVersionRepository questionnaireVersions,
                                    AssessmentQuestionRepository questions,
                                    AssessmentAnswerOptionRepository options) {
        this.questionnaireVersions = questionnaireVersions;
        this.questions = questions;
        this.options = options;
    }

    @Transactional(readOnly = true)
    public CatalogDto current() {
        AssessmentQuestionnaireVersion version = questionnaireVersions.findFirstByCurrentTrue()
                .orElseThrow(() -> ApiProblem.notFound("Cuestionario vigente"));
        return new CatalogDto(version.versionCode, questionsFor(version.id));
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> questionsFor(UUID questionnaireVersionId) {
        Map<String, List<AnswerOptionDto>> optionsByCompetency = options
                .findByIdQuestionnaireVersionIdOrderByIdCompetencyCodeAscIdLevelAsc(questionnaireVersionId)
                .stream()
                .collect(Collectors.groupingBy(option -> option.id.competencyCode,
                        LinkedHashMap::new,
                        Collectors.mapping(option -> new AnswerOptionDto(option.id.level, option.description), Collectors.toList())));

        return questions.findByIdQuestionnaireVersionIdOrderByPosition(questionnaireVersionId).stream()
                .map(question -> new QuestionDto(
                        question.id.competencyCode,
                        question.position,
                        question.title,
                        question.questionText,
                        List.copyOf(optionsByCompetency.getOrDefault(question.id.competencyCode, List.of()))))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean containsQuestion(UUID questionnaireVersionId, String competencyCode) {
        return questions.existsById(new AssessmentQuestionId(questionnaireVersionId, competencyCode));
    }
}
