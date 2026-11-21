package com.nextrole.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentMatrixVersionRepository extends JpaRepository<AssessmentMatrixVersion, UUID> {
    Optional<AssessmentMatrixVersion> findFirstByCurrentTrue();
}
