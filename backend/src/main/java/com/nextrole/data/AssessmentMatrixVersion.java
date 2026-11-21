package com.nextrole.data;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_matrix_versions")
public class AssessmentMatrixVersion {
    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "version_code", nullable = false, length = 32)
    public String versionCode;

    @Column(name = "published_at", nullable = false)
    public Instant publishedAt;

    @Column(name = "is_current", nullable = false)
    public boolean current;

    protected AssessmentMatrixVersion() {}
}
