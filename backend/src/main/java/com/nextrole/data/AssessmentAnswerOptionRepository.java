package com.nextrole.data;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnswerOptionRepository
        extends JpaRepository<AssessmentAnswerOption, AssessmentAnswerOptionId> {
    List<AssessmentAnswerOption> findByIdQuestionnaireVersionIdAndIdCompetencyCodeOrderByIdLevel(
            UUID questionnaireVersionId, String competencyCode);

    List<AssessmentAnswerOption> findByIdQuestionnaireVersionIdOrderByIdCompetencyCodeAscIdLevelAsc(
            UUID questionnaireVersionId);
}
