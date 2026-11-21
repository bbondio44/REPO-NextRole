package com.nextrole.data;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_answers")
public class AssessmentAnswer {
    @EmbeddedId
    public AssessmentAnswerId id;

    @Column(name = "questionnaire_version_id", nullable = false)
    public UUID questionnaireVersionId;

    @Column(name = "level", nullable = false)
    public short level;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public AssessmentAnswer(AssessmentAnswerId id, UUID questionnaireVersionId, short level, Instant updatedAt) {
        this.id = id;
        this.questionnaireVersionId = questionnaireVersionId;
        this.level = level;
        this.updatedAt = updatedAt;
    }

    protected AssessmentAnswer() {}
}
