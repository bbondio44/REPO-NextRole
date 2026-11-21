package com.nextrole.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, AssessmentQuestionId> {
    List<AssessmentQuestion> findByIdQuestionnaireVersionIdOrderByPosition(java.util.UUID questionnaireVersionId);
}
