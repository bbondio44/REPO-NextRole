package com.nextrole.data;

import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_role_results")
public class AssessmentRoleResult {
    @EmbeddedId
    public AssessmentRoleResultId id;

    @Column(name = "score_numerator", nullable = false)
    public BigInteger scoreNumerator;

    @Column(name = "score_denominator", nullable = false)
    public BigInteger scoreDenominator;

    @Column(name = "display_percentage", nullable = false, precision = 4, scale = 1)
    public BigDecimal displayPercentage;

    @Column(name = "rank_position", nullable = false)
    public short rankPosition;

    public AssessmentRoleResult(AssessmentRoleResultId id, BigInteger scoreNumerator, BigInteger scoreDenominator,
                                BigDecimal displayPercentage, short rankPosition) {
        this.id = id;
        this.scoreNumerator = scoreNumerator;
        this.scoreDenominator = scoreDenominator;
        this.displayPercentage = displayPercentage;
        this.rankPosition = rankPosition;
    }

    protected AssessmentRoleResult() {}
}
