package com.nextrole.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AssessmentRoleResultId implements Serializable {
    @Column(name = "result_set_id")
    public UUID resultSetId;

    @Column(name = "role_id")
    public String roleId;

    protected AssessmentRoleResultId() {}

    public AssessmentRoleResultId(UUID resultSetId, String roleId) {
        this.resultSetId = resultSetId;
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AssessmentRoleResultId that)) return false;
        return Objects.equals(resultSetId, that.resultSetId)
                && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultSetId, roleId);
    }
}
