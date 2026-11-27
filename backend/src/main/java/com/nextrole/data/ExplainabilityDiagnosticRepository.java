package com.nextrole.data;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Narrow persistence port: state-changing operations are compare-and-set and never overwrite SUCCEEDED. */
public interface ExplainabilityDiagnosticRepository extends Repository<ExplainabilityDiagnostic, UUID> {
    Optional<ExplainabilityDiagnostic> findByAttemptIdAndRoleId(UUID attemptId, String roleId);

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO explainability_diagnostics
              (id, attempt_id, role_id, status, generation_token, lease_until, attempt_count,
               role_name_snapshot, role_description_snapshot, created_at, updated_at)
            VALUES (:id, :attemptId, :roleId, 'GENERATING', :token, :leaseUntil, 1,
                    :roleName, :roleDescription, now(), now())
            ON CONFLICT (attempt_id, role_id) DO NOTHING
            """, nativeQuery = true)
    int createGenerating(@Param("id") UUID id, @Param("attemptId") UUID attemptId,
                         @Param("roleId") String roleId, @Param("token") UUID token,
                         @Param("leaseUntil") Instant leaseUntil, @Param("roleName") String roleName,
                         @Param("roleDescription") String roleDescription);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE explainability_diagnostics
               SET status='GENERATING', generation_token=:token, lease_until=:leaseUntil,
                   attempt_count=attempt_count + 1, failure_code=NULL, updated_at=now()
             WHERE attempt_id=:attemptId AND role_id=:roleId AND status='FAILED'
            """, nativeQuery = true)
    int claimFailed(@Param("attemptId") UUID attemptId, @Param("roleId") String roleId,
                    @Param("token") UUID token, @Param("leaseUntil") Instant leaseUntil);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE explainability_diagnostics
               SET status='GENERATING', generation_token=:token, lease_until=:leaseUntil,
                   attempt_count=attempt_count + 1, failure_code=NULL, updated_at=now()
             WHERE attempt_id=:attemptId AND role_id=:roleId AND status='GENERATING' AND lease_until <= :now
            """, nativeQuery = true)
    int claimExpired(@Param("attemptId") UUID attemptId, @Param("roleId") String roleId,
                     @Param("token") UUID token, @Param("leaseUntil") Instant leaseUntil,
                     @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE explainability_diagnostics
               SET status='SUCCEEDED', diagnosis_content=CAST(:content AS jsonb), provider=:provider,
                   model_requested=:modelRequested, model_served=:modelServed, prompt_version=:promptVersion,
                   prompt_hash=:promptHash, context_hash=:contextHash, duration_ms=:durationMs,
                   input_tokens=:inputTokens, output_tokens=:outputTokens, reasoning_tokens=:reasoningTokens,
                   provider_cost=:providerCost, role_name_snapshot=:roleName,
                   role_description_snapshot=:roleDescription, lease_until=NULL, failure_code=NULL,
                   succeeded_at=now(), updated_at=now()
             WHERE attempt_id=:attemptId AND role_id=:roleId AND status='GENERATING' AND generation_token=:token
            """, nativeQuery = true)
    int completeSuccess(@Param("attemptId") UUID attemptId, @Param("roleId") String roleId,
                        @Param("token") UUID token, @Param("content") String content,
                        @Param("provider") String provider, @Param("modelRequested") String modelRequested,
                        @Param("modelServed") String modelServed, @Param("promptVersion") String promptVersion,
                        @Param("promptHash") String promptHash, @Param("contextHash") String contextHash,
                        @Param("durationMs") Long durationMs, @Param("inputTokens") Long inputTokens,
                        @Param("outputTokens") Long outputTokens, @Param("reasoningTokens") Long reasoningTokens,
                        @Param("providerCost") java.math.BigDecimal providerCost,
                        @Param("roleName") String roleName, @Param("roleDescription") String roleDescription);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE explainability_diagnostics
               SET status='FAILED', lease_until=NULL, failure_code=:failureCode, updated_at=now()
             WHERE attempt_id=:attemptId AND role_id=:roleId AND status='GENERATING' AND generation_token=:token
            """, nativeQuery = true)
    int completeFailure(@Param("attemptId") UUID attemptId, @Param("roleId") String roleId,
                        @Param("token") UUID token, @Param("failureCode") String failureCode);
}
