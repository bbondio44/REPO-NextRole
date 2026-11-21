package com.nextrole.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AssessmentRoleRequirementId implements Serializable {
    @Column(name = "matrix_version_id")
    public UUID matrixVersionId;

    @Column(name = "role_id")
    public String roleId;

    @Column(name = "competency_code")
    public String competencyCode;

    protected AssessmentRoleRequirementId() {}

    public AssessmentRoleRequirementId(UUID matrixVersionId, String roleId, String competencyCode) {
        this.matrixVersionId = matrixVersionId;
        this.roleId = roleId;
        this.competencyCode = competencyCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AssessmentRoleRequirementId that)) return false;
        return Objects.equals(matrixVersionId, that.matrixVersionId)
                && Objects.equals(roleId, that.roleId)
                && Objects.equals(competencyCode, that.competencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrixVersionId, roleId, competencyCode);
    }
}
