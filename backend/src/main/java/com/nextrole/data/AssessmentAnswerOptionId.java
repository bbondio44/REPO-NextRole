package com.nextrole.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AssessmentAnswerOptionId implements Serializable {
    @Column(name = "questionnaire_version_id")
    public UUID questionnaireVersionId;

    @Column(name = "competency_code")
    public String competencyCode;

    @Column(name = "level")
    public Short level;

    protected AssessmentAnswerOptionId() {}

    public AssessmentAnswerOptionId(UUID questionnaireVersionId, String competencyCode, Short level) {
        this.questionnaireVersionId = questionnaireVersionId;
        this.competencyCode = competencyCode;
        this.level = level;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AssessmentAnswerOptionId that)) return false;
        return Objects.equals(questionnaireVersionId, that.questionnaireVersionId)
                && Objects.equals(competencyCode, that.competencyCode)
                && Objects.equals(level, that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionnaireVersionId, competencyCode, level);
    }
}
