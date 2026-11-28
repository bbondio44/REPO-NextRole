package com.nextrole.explainability;

import com.nextrole.data.ExplainabilityDiagnosticRepository;
import com.nextrole.data.ExplainabilityDiagnosticStatus;
import com.nextrole.explainability.support.FakeDiagnosisGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class ExplainabilityConcurrencyIntegrationTest {
    private static final UUID Q2 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID M1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String ROLE_ID = "qa-jr";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("explainability_concurrency").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        props.add("spring.datasource.username", POSTGRES::getUsername);
        props.add("spring.datasource.password", POSTGRES::getPassword);
        props.add("nextrole.security.jwt-secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
    }

    @Autowired ExplainabilityService service;
    @Autowired ExplainabilityDiagnosticRepository diagnostics;
    @Autowired FakeDiagnosisGenerator fake;
    @Autowired JdbcTemplate jdbc;
    private ExecutorService executor;
    private UUID userId;
    private UUID attemptId;

    @BeforeEach
    void seedCompletedAttempt() {
        executor = Executors.newFixedThreadPool(2);
        fake.reset();
        userId = UUID.randomUUID();
        attemptId = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,first_name,last_name,password_hash,role) VALUES (?, ?, 'Fixture', 'User', 'hash', 'STUDENT')",
                userId, "explainability-race-" + userId + "@example.test");
        jdbc.update("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, 'COMPLETED', ?, ?, now(), now())
                """, attemptId, userId, Q2, M1);
        jdbc.update("""
                INSERT INTO assessment_answers(attempt_id,questionnaire_version_id,competency_code,level)
                SELECT ?, ?, competency_code, 1 FROM assessment_questions WHERE questionnaire_version_id=?
                """, attemptId, Q2, Q2);
        jdbc.update("INSERT INTO assessment_result_sets(attempt_id,algorithm_version) VALUES (?, 'matching-v2.0')", attemptId);
        jdbc.update("""
                INSERT INTO assessment_role_results(result_set_id,role_id,score_numerator,score_denominator,display_percentage,rank_position)
                SELECT ?, id, 555, 1000, 55.5, row_number() OVER (ORDER BY id)::smallint FROM assessment_roles
                """, attemptId);
    }

    @AfterEach
    void stopExecutor() {
        if (executor != null) executor.shutdownNow();
    }

    @Test
    void concurrentInitialRequestsClaimOneRowAndCallProviderOnce() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        fake.delayNextInvocation(entered, release);

        Future<ExplainabilityService.GenerationOutcome> first = executor.submit(
                () -> service.generateInitial(userId, attemptId, ROLE_ID));
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS), "first call reached the fake provider");
            var second = service.generateInitial(userId, attemptId, ROLE_ID);
            assertEquals(ExplainabilityDiagnosticStatus.GENERATING, second.status());
            assertEquals(1, fake.invocationCount());
        } finally {
            release.countDown();
        }

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, first.get(10, TimeUnit.SECONDS).status());
        var saved = diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow();
        assertEquals(1, saved.attemptCount);
        assertEquals(1, fake.invocationCount());
    }

    @Test
    void concurrentManualRetriesClaimFailedRowOnce() throws Exception {
        UUID firstToken = seedFailedRow();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        fake.delayNextInvocation(entered, release);

        Future<ExplainabilityService.GenerationOutcome> first = executor.submit(
                () -> service.retryManual(userId, attemptId, ROLE_ID));
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS), "first retry reached the fake provider");
            var second = service.retryManual(userId, attemptId, ROLE_ID);
            assertEquals(ExplainabilityDiagnosticStatus.GENERATING, second.status());
            assertEquals(1, fake.invocationCount());
        } finally {
            release.countDown();
        }

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, first.get(10, TimeUnit.SECONDS).status());
        var saved = diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow();
        assertEquals(2, saved.attemptCount);
        assertNotEquals(firstToken, saved.generationToken);
        assertEquals(1, fake.invocationCount());
    }

    @Test
    void expiredGetDoesNotPersistRecoveryAndManualRetryFencesTheLateProviderResponse() throws Exception {
        CountDownLatch oldProviderEntered = new CountDownLatch(1);
        CountDownLatch releaseOldProvider = new CountDownLatch(1);
        fake.delayNextInvocation(oldProviderEntered, releaseOldProvider);
        Future<ExplainabilityService.GenerationOutcome> oldGeneration = executor.submit(
                () -> service.generateInitial(userId, attemptId, ROLE_ID));
        try {
            assertTrue(oldProviderEntered.await(10, TimeUnit.SECONDS), "generation reached delayed provider");
            var before = diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow();
            UUID staleToken = before.generationToken;
            jdbc.update("UPDATE explainability_diagnostics SET lease_until=? WHERE attempt_id=? AND role_id=?",
                    Timestamp.from(Instant.now().minusSeconds(1)), attemptId, ROLE_ID);

            var read = service.getStatus(userId, attemptId, ROLE_ID);
            assertEquals(ExplainabilityDiagnosticStatus.FAILED, read.status());
            assertEquals("GENERATION_INTERRUPTED", read.failureCode());
            assertEquals("GENERATING", diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow().status.name(),
                    "GET derives interruption without updating the database");

            var retried = service.retryManual(userId, attemptId, ROLE_ID);
            assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, retried.status());
            var success = diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow();
            UUID newToken = success.generationToken;
            assertNotEquals(staleToken, newToken);
            assertEquals(2, success.attemptCount);

            releaseOldProvider.countDown();
            assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, oldGeneration.get(10, TimeUnit.SECONDS).status());
            var unchanged = diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow();
            assertEquals(newToken, unchanged.generationToken);
            assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, unchanged.status);
            assertEquals(2, unchanged.attemptCount);
        } finally {
            releaseOldProvider.countDown();
        }
    }

    private UUID seedFailedRow() {
        UUID token = UUID.randomUUID();
        assertEquals(1, diagnostics.createGenerating(UUID.randomUUID(), attemptId, ROLE_ID, token,
                Instant.now().plusSeconds(120), "QA", "QA historical snapshot"));
        assertEquals(1, diagnostics.completeFailure(attemptId, ROLE_ID, token, "PROVIDER_UNAVAILABLE"));
        return token;
    }

    @TestConfiguration
    static class FakeProviderConfiguration {
        @Bean @Primary FakeDiagnosisGenerator fakeDiagnosisGenerator() { return new FakeDiagnosisGenerator(); }
    }
}
