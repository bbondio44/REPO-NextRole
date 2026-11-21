package com.nextrole.data;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_result_sets")
public class AssessmentResultSet {
    @Id
    @Column(name = "attempt_id")
    public UUID attemptId;

    @Column(name = "algorithm_version", nullable = false, length = 50)
    public String algorithmVersion;

    @Column(name = "calculated_at", nullable = false)
    public Instant calculatedAt;

    protected AssessmentResultSet() {}
}
