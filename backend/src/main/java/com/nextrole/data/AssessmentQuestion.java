package com.nextrole.data;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {
    @EmbeddedId
    public AssessmentQuestionId id;

    @Column(name = "position", nullable = false)
    public short position;

    @Column(name = "title", nullable = false, length = 180)
    public String title;

    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    public String questionText;

    protected AssessmentQuestion() {}
}
