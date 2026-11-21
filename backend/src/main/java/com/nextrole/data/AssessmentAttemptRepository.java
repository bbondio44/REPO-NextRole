package com.nextrole.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    Optional<AssessmentAttempt> findFirstByUserIdAndStatus(UUID userId, AssessmentAttemptStatus status);
    Optional<AssessmentAttempt> findByIdAndUserId(UUID id, UUID userId);
}
