package com.nextrole.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentQuestionnaireVersionRepository
        extends JpaRepository<AssessmentQuestionnaireVersion, UUID> {
    Optional<AssessmentQuestionnaireVersion> findFirstByCurrentTrue();
}
