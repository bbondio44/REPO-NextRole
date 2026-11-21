package com.nextrole.data;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRoleRequirementRepository
        extends JpaRepository<AssessmentRoleRequirement, AssessmentRoleRequirementId> {
    List<AssessmentRoleRequirement> findByIdMatrixVersionId(UUID matrixVersionId);
}
