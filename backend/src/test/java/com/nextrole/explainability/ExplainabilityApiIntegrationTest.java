package com.nextrole.explainability;

import com.nextrole.data.ExplainabilityDiagnosticRepository;
import com.nextrole.explainability.support.FakeDiagnosisGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExplainabilityApiIntegrationTest {
    private static final UUID Q1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID M1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID Q2 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("explainability_api").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        props.add("spring.datasource.username", POSTGRES::getUsername);
        props.add("spring.datasource.password", POSTGRES::getPassword);
        props.add("nextrole.security.jwt-secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
        props.add("nextrole.explainability.openrouter-api-key", () -> "");
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired JdbcTemplate jdbc;
    @Autowired ExplainabilityDiagnosticRepository diagnostics;
    @Autowired FakeDiagnosisGenerator fake;
    @Autowired ExplainabilityProperties properties;

    @BeforeEach
    void resetFake() {
        fake.reset();
        properties.setModel("baidu/ernie-5.5-flash-preview:free");
        properties.setPromptVersion("explainability-v5");
    }

    @Test
    void getIsAuthenticatedPureAndNeverStartsGeneration() {
        Account owner = register("explainability-api-" + UUID.randomUUID() + "@example.test");
        UUID attemptId = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        String endpoint = diagnosisPath(attemptId, "qa-jr");

        assertEquals(HttpStatus.UNAUTHORIZED, rest.getForEntity(url(endpoint), Map.class).getStatusCode());
        ResponseEntity<Map> response = rest.exchange(url(endpoint), HttpMethod.GET,
                new HttpEntity<>(owner.headers()), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NOT_STARTED", response.getBody().get("status"));
        assertEquals(attemptId.toString(), response.getBody().get("attemptId").toString());
        assertEquals("qa-jr", response.getBody().get("roleId"));
        assertTrue(diagnostics.findByAttemptIdAndRoleId(attemptId, "qa-jr").isEmpty());
        assertEquals(0, fake.invocationCount());
    }

    @Test
    void diagnosisUsesRequestedQ1AndQ2AttemptAndReturnsSavedRoleSnapshots() {
        Account owner = register("explainability-history-" + UUID.randomUUID() + "@example.test");
        UUID q1Attempt = createAttempt(owner.userId(), Q1, "COMPLETED", 2, 70.0);
        UUID q2Attempt = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);

        ResponseEntity<Map> q1Generated = post(owner.headers(), diagnosisPath(q1Attempt, "backend-dev-jr") + "/generate");
        var q1Context = fake.lastContext();
        ResponseEntity<Map> q2Generated = post(owner.headers(), diagnosisPath(q2Attempt, "backend-dev-jr") + "/generate");
        var q2Context = fake.lastContext();

        assertEquals(HttpStatus.OK, q1Generated.getStatusCode());
        assertEquals(HttpStatus.OK, q2Generated.getStatusCode());
        assertEquals("SUCCEEDED", q1Generated.getBody().get("status"));
        assertEquals("SUCCEEDED", q2Generated.getBody().get("status"));
        assertEquals("Backend Developer", q1Generated.getBody().get("roleName"));
        assertEquals(70.0, q1Context.matchingPercentage().doubleValue());
        assertEquals(55.5, q2Context.matchingPercentage().doubleValue());
        var q1Rest = q1Context.competencies().stream().filter(item -> item.id().equals("rest-api-consumption")).findFirst().orElseThrow();
        var q2Rest = q2Context.competencies().stream().filter(item -> item.id().equals("rest-api-consumption")).findFirst().orElseThrow();
        assertEquals(2, q1Rest.current().level());
        assertEquals("Puedo realizar solicitudes a una API existente, enviar parámetros sencillos y utilizar las respuestas obtenidas.",
                q1Rest.current().description());
        assertEquals(1, q2Rest.current().level());
        assertEquals("Comprendo cómo una aplicación realiza solicitudes HTTP a una API REST existente y utiliza las respuestas obtenidas.",
                q2Rest.current().description());
        String snapshot = q1Generated.getBody().get("roleDescription").toString();
        assertEquals(snapshot, q2Generated.getBody().get("roleDescription"));
        assertTrue(((Map<?, ?>) q1Generated.getBody().get("diagnosis")).containsKey("currentSituation"));
        assertEquals(2, fake.invocationCount());
        assertNotEquals(diagnostics.findByAttemptIdAndRoleId(q1Attempt, "backend-dev-jr").orElseThrow().contextHash.trim(),
                diagnostics.findByAttemptIdAndRoleId(q2Attempt, "backend-dev-jr").orElseThrow().contextHash.trim());

        jdbc.update("UPDATE assessment_roles SET name='Backend renamed', description='Changed current copy' WHERE id='backend-dev-jr'");
        properties.setModel("a-later-model");
        properties.setPromptVersion("unknown-future-prompt");
        ResponseEntity<Map> q1Read = rest.exchange(url(diagnosisPath(q1Attempt, "backend-dev-jr")), HttpMethod.GET,
                new HttpEntity<>(owner.headers()), Map.class);
        ResponseEntity<Map> repeatedGenerate = post(owner.headers(), diagnosisPath(q1Attempt, "backend-dev-jr") + "/generate");

        assertEquals(HttpStatus.OK, q1Read.getStatusCode());
        assertEquals("Backend Developer", q1Read.getBody().get("roleName"));
        assertEquals(snapshot, q1Read.getBody().get("roleDescription"));
        assertEquals(snapshot, repeatedGenerate.getBody().get("roleDescription"));
        assertEquals("fake", repeatedGenerate.getBody().get("model"));
        assertEquals("explainability-v5", repeatedGenerate.getBody().get("promptVersion"));
        assertEquals(2, fake.invocationCount(), "a successful historical diagnosis is immutable and reused");
    }

    @Test
    void hidesForeignAttemptsAndRejectsIncompleteAttemptsAndUnknownRoles() {
        Account owner = register("explainability-owner-" + UUID.randomUUID() + "@example.test");
        Account other = register("explainability-other-" + UUID.randomUUID() + "@example.test");
        UUID completed = createAttempt(owner.userId(), Q2, "COMPLETED", 2, 70.0);
        UUID draft = createAttempt(owner.userId(), Q2, "DRAFT", 2, 70.0);

        ResponseEntity<Map> foreign = rest.exchange(url(diagnosisPath(completed, "qa-jr")), HttpMethod.GET,
                new HttpEntity<>(other.headers()), Map.class);
        ResponseEntity<Map> incomplete = post(owner.headers(), diagnosisPath(draft, "qa-jr") + "/generate");
        ResponseEntity<Map> unknownRole = rest.exchange(url(diagnosisPath(completed, "not-a-role")), HttpMethod.GET,
                new HttpEntity<>(owner.headers()), Map.class);

        assertEquals(HttpStatus.NOT_FOUND, foreign.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, incomplete.getStatusCode());
        assertEquals("ATTEMPT_NOT_COMPLETED", incomplete.getBody().get("code"));
        assertEquals(HttpStatus.NOT_FOUND, unknownRole.getStatusCode());
        assertEquals(0, fake.invocationCount());
    }

    @Test
    void getDerivesExpiredGenerationAsRetryableWithoutWritingOrCallingProvider() {
        Account owner = register("explainability-expired-" + UUID.randomUUID() + "@example.test");
        UUID attemptId = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        UUID token = UUID.randomUUID();
        assertEquals(1, diagnostics.createGenerating(UUID.randomUUID(), attemptId, "qa-jr", token,
                Instant.now().minusSeconds(1), "QA", "QA snapshot"));

        ResponseEntity<Map> response = rest.exchange(url(diagnosisPath(attemptId, "qa-jr")), HttpMethod.GET,
                new HttpEntity<>(owner.headers()), Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FAILED", response.getBody().get("status"));
        assertEquals("GENERATION_INTERRUPTED", response.getBody().get("failureCode"));
        assertEquals(Boolean.TRUE, response.getBody().get("canRetry"));
        var persisted = diagnostics.findByAttemptIdAndRoleId(attemptId, "qa-jr").orElseThrow();
        assertEquals("GENERATING", persisted.status.name());
        assertEquals(1, persisted.attemptCount);
        assertEquals(token, persisted.generationToken);
        assertEquals(0, fake.invocationCount());
    }

    @Test
    void generateRequiresExplicitRetryForFailedRowsAndRetryCanCompleteThem() {
        Account owner = register("explainability-retry-" + UUID.randomUUID() + "@example.test");
        UUID attemptId = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        UUID token = UUID.randomUUID();
        assertEquals(1, diagnostics.createGenerating(UUID.randomUUID(), attemptId, "qa-jr", token,
                Instant.now().plusSeconds(120), "QA", "QA snapshot"));
        assertEquals(1, diagnostics.completeFailure(attemptId, "qa-jr", token, "PROVIDER_UNAVAILABLE"));

        ResponseEntity<Map> generate = post(owner.headers(), diagnosisPath(attemptId, "qa-jr") + "/generate");
        assertEquals(HttpStatus.CONFLICT, generate.getStatusCode());
        assertEquals("DIAGNOSIS_RETRY_REQUIRED", generate.getBody().get("code"));
        assertEquals(0, fake.invocationCount());

        ResponseEntity<Map> retried = post(owner.headers(), diagnosisPath(attemptId, "qa-jr") + "/retry");
        assertEquals(HttpStatus.OK, retried.getStatusCode());
        assertEquals("SUCCEEDED", retried.getBody().get("status"));
        assertEquals(2, diagnostics.findByAttemptIdAndRoleId(attemptId, "qa-jr").orElseThrow().attemptCount);
        assertEquals(1, fake.invocationCount());

        ResponseEntity<Map> retrySuccess = post(owner.headers(), diagnosisPath(attemptId, "qa-jr") + "/retry");
        assertEquals(HttpStatus.CONFLICT, retrySuccess.getStatusCode());
        assertEquals("DIAGNOSIS_ALREADY_SUCCEEDED", retrySuccess.getBody().get("code"));
        assertEquals(1, fake.invocationCount());
    }

    @Test
    void activeGenerationReturnsAcceptedForGenerateAndRetryWithoutDuplicateCalls() {
        Account owner = register("explainability-active-" + UUID.randomUUID() + "@example.test");
        UUID attemptId = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        assertEquals(1, diagnostics.createGenerating(UUID.randomUUID(), attemptId, "qa-jr", UUID.randomUUID(),
                Instant.now().plusSeconds(120), "QA", "QA snapshot"));

        ResponseEntity<Map> generate = post(owner.headers(), diagnosisPath(attemptId, "qa-jr") + "/generate");
        ResponseEntity<Map> retry = post(owner.headers(), diagnosisPath(attemptId, "qa-jr") + "/retry");

        assertEquals(HttpStatus.ACCEPTED, generate.getStatusCode());
        assertEquals(2, ((Number) generate.getBody().get("retryAfterSeconds")).intValue());
        assertEquals(HttpStatus.ACCEPTED, retry.getStatusCode());
        assertEquals(0, fake.invocationCount());
        assertEquals(1, diagnostics.findByAttemptIdAndRoleId(attemptId, "qa-jr").orElseThrow().attemptCount);
    }

    @Test
    void providerRateLimitTimeoutAndInvalidOutputMapToSafeHttpErrorsAndPersistFailures() {
        Account owner = register("explainability-errors-" + UUID.randomUUID() + "@example.test");
        UUID unavailableAttempt = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        fake.setScenario(FakeDiagnosisGenerator.Scenario.RATE_LIMITED);
        ResponseEntity<Map> unavailable = post(owner.headers(), diagnosisPath(unavailableAttempt, "qa-jr") + "/generate");
        var failedRow = diagnostics.findByAttemptIdAndRoleId(unavailableAttempt, "qa-jr").orElse(null);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, unavailable.getStatusCode(),
                String.valueOf(unavailable.getBody()) + "; fakeCalls=" + fake.invocationCount() + "; persisted=" + (failedRow == null ? "none" : failedRow.status + "/" + failedRow.failureCode));
        assertEquals("PROVIDER_UNAVAILABLE", unavailable.getBody().get("code"));
        assertFalse(unavailable.getBody().toString().contains("raw"));
        assertEquals("FAILED", diagnostics.findByAttemptIdAndRoleId(unavailableAttempt, "qa-jr").orElseThrow().status.name());

        fake.setScenario(FakeDiagnosisGenerator.Scenario.TIMEOUT);
        UUID timeoutAttempt = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        ResponseEntity<Map> timeout = post(owner.headers(), diagnosisPath(timeoutAttempt, "qa-jr") + "/generate");
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, timeout.getStatusCode());
        assertEquals("PROVIDER_TIMEOUT", timeout.getBody().get("code"));

        fake.setScenario(FakeDiagnosisGenerator.Scenario.INVALID_RESPONSE);
        UUID invalidAttempt = createAttempt(owner.userId(), Q2, "COMPLETED", 1, 55.5);
        ResponseEntity<Map> invalid = post(owner.headers(), diagnosisPath(invalidAttempt, "qa-jr") + "/generate");
        assertEquals(HttpStatus.BAD_GATEWAY, invalid.getStatusCode());
        assertEquals("INVALID_PROVIDER_RESPONSE", invalid.getBody().get("code"));
        assertFalse(invalid.getBody().toString().contains("not valid JSON"));
        assertEquals(3, fake.invocationCount());
    }

    private Account register(String email) {
        Map<String, Object> payload = Map.of("firstName", "Test", "lastName", "User", "email", email,
                "password", "Segura-12345!", "confirmPassword", "Segura-12345!", "acceptTerms", true);
        ResponseEntity<Map> response = rest.postForEntity(url("/api/v1/auth/register"), payload, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(response.getBody().get("accessToken").toString());
        UUID userId = jdbc.queryForObject("SELECT id FROM users WHERE email=?", UUID.class, email);
        return new Account(userId, headers);
    }

    private UUID createAttempt(UUID userId, UUID questionnaire, String status, int level, double percentage) {
        UUID attemptId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, ?, ?, ?, now(), CASE WHEN ?='COMPLETED' THEN now() ELSE NULL END)
                """, attemptId, userId, status, questionnaire, M1, status);
        if ("COMPLETED".equals(status)) {
            jdbc.update("""
                    INSERT INTO assessment_answers(attempt_id,questionnaire_version_id,competency_code,level)
                    SELECT ?, ?, competency_code, ? FROM assessment_questions WHERE questionnaire_version_id=?
                    """, attemptId, questionnaire, level, questionnaire);
            jdbc.update("INSERT INTO assessment_result_sets(attempt_id,algorithm_version) VALUES (?, 'matching-v2.0')", attemptId);
            BigDecimalValues score = new BigDecimalValues(percentage);
            jdbc.update("""
                    INSERT INTO assessment_role_results(result_set_id,role_id,score_numerator,score_denominator,display_percentage,rank_position)
                    SELECT ?, id, ?, ?, ?, row_number() OVER (ORDER BY id)::smallint FROM assessment_roles
                    """, attemptId, score.numerator(), score.denominator(), score.value());
        }
        return attemptId;
    }

    private ResponseEntity<Map> post(HttpHeaders headers, String path) {
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(headers), Map.class);
    }

    private static String diagnosisPath(UUID attemptId, String roleId) {
        return "/api/v2/assessment-attempts/" + attemptId + "/diagnoses/" + roleId;
    }

    private String url(String path) { return "http://localhost:" + port + path; }

    private record Account(UUID userId, HttpHeaders headers) {}
    private record BigDecimalValues(double value) {
        long numerator() { return Math.round(value * 10); }
        long denominator() { return 1000; }
    }

    @TestConfiguration
    static class FakeProviderConfiguration {
        @Bean @Primary FakeDiagnosisGenerator fakeDiagnosisGenerator() { return new FakeDiagnosisGenerator(); }
    }
}
