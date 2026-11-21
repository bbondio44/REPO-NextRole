package com.nextrole.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AssessmentAnswerId implements Serializable {
    @Column(name = "attempt_id")
    public UUID attemptId;

    @Column(name = "competency_code")
    public String competencyCode;

    protected AssessmentAnswerId() {}

    public AssessmentAnswerId(UUID attemptId, String competencyCode) {
        this.attemptId = attemptId;
        this.competencyCode = competencyCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AssessmentAnswerId that)) return false;
        return Objects.equals(attemptId, that.attemptId)
                && Objects.equals(competencyCode, that.competencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attemptId, competencyCode);
    }
}
