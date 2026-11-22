package com.nextrole.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, UUID> {
    Optional<AssessmentAttempt> findFirstByUserIdAndStatus(UUID userId, AssessmentAttemptStatus status);
    Optional<AssessmentAttempt> findByIdAndUserId(UUID id, UUID userId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from AssessmentAttempt attempt where attempt.id = :attemptId and attempt.userId = :userId")
    Optional<AssessmentAttempt> findByIdAndUserIdForUpdate(@Param("attemptId") UUID attemptId,
                                                           @Param("userId") UUID userId);
}
