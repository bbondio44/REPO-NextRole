package com.nextrole.data;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_role_requirements")
public class AssessmentRoleRequirement {
    @EmbeddedId
    public AssessmentRoleRequirementId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 24)
    public AssessmentRequirementImportance importance;

    @Column(name = "expected_level")
    public Short expectedLevel;

    protected AssessmentRoleRequirement() {}
}
