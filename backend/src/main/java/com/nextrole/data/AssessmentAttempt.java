package com.nextrole.data;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "assessment_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_assessment_attempt_id_questionnaire", columnNames = {"id", "questionnaire_version_id"})
})
public class AssessmentAttempt {
    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    public AssessmentAttemptStatus status;

    @Column(name = "questionnaire_version_id", nullable = false)
    public UUID questionnaireVersionId;

    @Column(name = "matrix_version_id", nullable = false)
    public UUID matrixVersionId;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "completed_at")
    public Instant completedAt;

    @Column(name = "discarded_at")
    public Instant discardedAt;

    @Column(name = "source_attempt_id")
    public UUID sourceAttemptId;

    public AssessmentAttempt(UUID id, UUID userId, AssessmentAttemptStatus status,
                             UUID questionnaireVersionId, UUID matrixVersionId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.questionnaireVersionId = questionnaireVersionId;
        this.matrixVersionId = matrixVersionId;
        this.createdAt = createdAt;
    }

    protected AssessmentAttempt() {}
}
