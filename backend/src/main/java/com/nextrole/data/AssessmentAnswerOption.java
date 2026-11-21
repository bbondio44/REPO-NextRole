package com.nextrole.data;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_answer_options")
public class AssessmentAnswerOption {
    @EmbeddedId
    public AssessmentAnswerOptionId id;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    public String description;

    protected AssessmentAnswerOption() {}
}
