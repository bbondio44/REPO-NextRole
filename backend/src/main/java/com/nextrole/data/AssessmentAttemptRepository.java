package com.nextrole.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    Optional<AssessmentAttempt> findFirstByUserIdAndStatus(UUID userId, AssessmentAttemptStatus status);
}
