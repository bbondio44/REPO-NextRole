package com.nextrole.data;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, AssessmentAnswerId> {
    List<AssessmentAnswer> findByIdAttemptId(UUID attemptId);
    long countByIdAttemptId(UUID attemptId);
    List<AssessmentAnswer> findByIdAttemptIdOrderByIdCompetencyCode(UUID attemptId);
}
