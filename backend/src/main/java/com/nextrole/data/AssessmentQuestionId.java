package com.nextrole.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AssessmentQuestionId implements Serializable {
    @Column(name = "questionnaire_version_id")
    public UUID questionnaireVersionId;

    @Column(name = "competency_code")
    public String competencyCode;

    protected AssessmentQuestionId() {}

    public AssessmentQuestionId(UUID questionnaireVersionId, String competencyCode) {
        this.questionnaireVersionId = questionnaireVersionId;
        this.competencyCode = competencyCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AssessmentQuestionId that)) return false;
        return Objects.equals(questionnaireVersionId, that.questionnaireVersionId)
                && Objects.equals(competencyCode, that.competencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionnaireVersionId, competencyCode);
    }
}
