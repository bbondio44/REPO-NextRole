package com.nextrole.data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "explainability_diagnostics")
public class ExplainabilityDiagnostic {
    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "attempt_id", nullable = false)
    public UUID attemptId;

    @Column(name = "role_id", nullable = false, length = 100)
    public String roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    public ExplainabilityDiagnosticStatus status;

    @Column(name = "generation_token")
    public UUID generationToken;

    @Column(name = "lease_until")
    public Instant leaseUntil;

    @Column(name = "attempt_count", nullable = false)
    public int attemptCount;

    @Column(name = "role_name_snapshot", length = 180)
    public String roleNameSnapshot;

    @Column(name = "role_description_snapshot", columnDefinition = "text")
    public String roleDescriptionSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "diagnosis_content", columnDefinition = "jsonb")
    public JsonNode diagnosisContent;

    @Column(name = "provider", length = 40)
    public String provider;

    @Column(name = "model_requested", length = 180)
    public String modelRequested;

    @Column(name = "model_served", length = 180)
    public String modelServed;

    @Column(name = "prompt_version", length = 80)
    public String promptVersion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "prompt_hash", columnDefinition = "char(64)")
    public String promptHash;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "context_hash", columnDefinition = "char(64)")
    public String contextHash;

    @Column(name = "duration_ms")
    public Long durationMs;

    @Column(name = "input_tokens")
    public Long inputTokens;

    @Column(name = "output_tokens")
    public Long outputTokens;

    @Column(name = "reasoning_tokens")
    public Long reasoningTokens;

    @Column(name = "provider_cost", precision = 20, scale = 10)
    public BigDecimal providerCost;

    @Column(name = "failure_code", length = 60)
    public String failureCode;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "succeeded_at")
    public Instant succeededAt;

    protected ExplainabilityDiagnostic() {}
}
