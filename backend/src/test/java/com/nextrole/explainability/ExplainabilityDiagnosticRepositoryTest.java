package com.nextrole.explainability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextrole.data.ExplainabilityDiagnosticRepository;
import com.nextrole.data.ExplainabilityDiagnosticStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class ExplainabilityDiagnosticRepositoryTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("explainability_repository").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        props.add("spring.datasource.username", POSTGRES::getUsername);
        props.add("spring.datasource.password", POSTGRES::getPassword);
        props.add("nextrole.security.jwt-secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
    }

    @Autowired ExplainabilityDiagnosticRepository repository;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    private UUID attempt;

    @BeforeEach
    void seedCompletedAttempt() {
        attempt = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,first_name,last_name,password_hash,role) VALUES (?, ?, 'Test', 'User', 'hash', 'STUDENT')",
                user, "explainability-repository-" + user + "@example.test");
        jdbc.update("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, 'COMPLETED', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', now(), now())
                """, attempt, user);
    }

    @Test
    void usesUniqueClaimsFencingJsonbSnapshotsAndTerminalSuccessfulState() throws Exception {
        String roleId = "qa-jr";
        UUID firstToken = UUID.randomUUID();
        Instant lease = Instant.now().plusSeconds(120);
        assertEquals(1, repository.createGenerating(UUID.randomUUID(), attempt, roleId, firstToken, lease, "QA", "QA snapshot"));
        assertEquals(0, repository.createGenerating(UUID.randomUUID(), attempt, roleId, UUID.randomUUID(), lease, "Other", "Other"));
        var generating = repository.findByAttemptIdAndRoleId(attempt, roleId).orElseThrow();
        assertEquals(ExplainabilityDiagnosticStatus.GENERATING, generating.status);
        assertEquals(firstToken, generating.generationToken);
        assertEquals(1, generating.attemptCount);

        assertEquals(1, repository.completeFailure(attempt, roleId, firstToken, "PROVIDER_ERROR"));
        assertEquals(0, repository.completeSuccess(attempt, roleId, firstToken, "{}", "openrouter", "model", null,
                "explainability-v5", "a".repeat(64), "b".repeat(64), 10L, 12L, 4L, null,
                BigDecimal.ZERO, "QA", "QA snapshot"));

        UUID secondToken = UUID.randomUUID();
        assertEquals(1, repository.claimFailed(attempt, roleId, secondToken, Instant.now().plusSeconds(120)));
        assertEquals(0, repository.completeSuccess(attempt, roleId, firstToken, "{}", "openrouter", "model", null,
                "explainability-v5", "a".repeat(64), "b".repeat(64), 10L, 12L, 4L, null,
                BigDecimal.ZERO, "QA", "QA snapshot"), "a stale generation must be fenced");

        String diagnosis = "{\"currentSituation\":\"Fortaleza\",\"recommendedFocus\":\"\u00c1rea\",\"importanceExplanation\":\"Raz\u00f3n\",\"professionalRelevance\":\"Rol\",\"highlightedCompetencyIds\":[\"api-testing\"]}";
        assertEquals(1, repository.completeSuccess(attempt, roleId, secondToken, diagnosis, "openrouter", "model", "served-model",
                "explainability-v5", "a".repeat(64), "b".repeat(64), 10L, 12L, 4L, 1L,
                new BigDecimal("0.0000000000"), "QA", "QA snapshot"));
        var success = repository.findByAttemptIdAndRoleId(attempt, roleId).orElseThrow();
        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, success.status);
        assertEquals("QA snapshot", success.roleDescriptionSnapshot);
        assertEquals("QA", success.roleNameSnapshot);
        assertNotNull(success.succeededAt);
        assertEquals(2, success.attemptCount);
        assertTrue(success.diagnosisContent.isObject());
        assertEquals("Fortaleza", success.diagnosisContent.get("currentSituation").asText());
        assertEquals(0, repository.completeSuccess(attempt, roleId, secondToken,
                "{\"currentSituation\":\"replacement\"}", "openrouter", "changed-model", null,
                "changed-prompt", "c".repeat(64), "d".repeat(64), 999L, null, null, null,
                null, "Replacement", "Replacement snapshot"));
        var unchanged = repository.findByAttemptIdAndRoleId(attempt, roleId).orElseThrow();
        assertEquals("QA snapshot", unchanged.roleDescriptionSnapshot);
        assertEquals("Fortaleza", unchanged.diagnosisContent.get("currentSituation").asText());
        assertEquals(0, repository.claimFailed(attempt, roleId, UUID.randomUUID(), lease));
        assertEquals(0, repository.claimExpired(attempt, roleId, UUID.randomUUID(), lease, Instant.now().plusSeconds(10)));
        assertEquals("QA snapshot", repository.findByAttemptIdAndRoleId(attempt, roleId).orElseThrow().roleDescriptionSnapshot);
    }

    @Test
    void expiredLeaseCanBeClaimedOnlyByCompareAndSetAndStaleTokenCannotComplete() {
        String roleId = "data-analyst-jr";
        UUID oldToken = UUID.randomUUID();
        assertEquals(1, repository.createGenerating(UUID.randomUUID(), attempt, roleId, oldToken,
                Instant.now().minusSeconds(1), "Data Analyst", "Data snapshot"));
        UUID newToken = UUID.randomUUID();
        Instant now = Instant.now();
        assertEquals(1, repository.claimExpired(attempt, roleId, newToken, now.plusSeconds(120), now));
        assertEquals(0, repository.claimExpired(attempt, roleId, UUID.randomUUID(), now.plusSeconds(120), now));
        assertEquals(0, repository.completeFailure(attempt, roleId, oldToken, "STALE"));
        assertEquals(1, repository.completeFailure(attempt, roleId, newToken, "INTERRUPTED"));
        var failed = repository.findByAttemptIdAndRoleId(attempt, roleId).orElseThrow();
        assertEquals(ExplainabilityDiagnosticStatus.FAILED, failed.status);
        assertNull(failed.diagnosisContent);
    }
}
