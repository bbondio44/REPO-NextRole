package com.nextrole.explainability;

import com.nextrole.data.ExplainabilityDiagnosticRepository;
import com.nextrole.data.ExplainabilityDiagnosticStatus;
import com.nextrole.explainability.support.FakeDiagnosisGenerator;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class ExplainabilityServiceIntegrationTest {
    private static final UUID Q1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID Q2 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID M1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final String ROLE_ID = "qa-jr";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("explainability_service").withUsername("nextrole").withPassword("test_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        props.add("spring.datasource.username", POSTGRES::getUsername);
        props.add("spring.datasource.password", POSTGRES::getPassword);
        props.add("nextrole.security.jwt-secret", () -> java.util.Base64.getEncoder().encodeToString(new byte[48]));
    }

    @Autowired private ExplainabilityService service;
    @Autowired private ExplainabilityDiagnosticRepository diagnostics;
    @Autowired private FakeDiagnosisGenerator fake;
    @Autowired private JdbcTemplate jdbc;
    private UUID userId;
    private UUID attemptId;

    @BeforeEach
    void seedVersionedCompletedAttempt() {
        fake.reset();
        userId = UUID.randomUUID();
        attemptId = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,first_name,last_name,password_hash,role) VALUES (?, ?, 'Fixture', 'User', 'hash', 'STUDENT')",
                userId, "explainability-service-" + userId + "@example.test");
        jdbc.update("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, 'COMPLETED', ?, ?, now(), now())
                """, attemptId, userId, Q1, M1);
        jdbc.update("""
                INSERT INTO assessment_answers(attempt_id,questionnaire_version_id,competency_code,level)
                SELECT ?, ?, competency_code, 2 FROM assessment_questions WHERE questionnaire_version_id=?
                """, attemptId, Q1, Q1);
        jdbc.update("INSERT INTO assessment_result_sets(attempt_id,algorithm_version) VALUES (?, 'matching-v2.0')",
                attemptId);
        jdbc.update("""
                INSERT INTO assessment_role_results(result_set_id,role_id,score_numerator,score_denominator,display_percentage,rank_position)
                SELECT ?, id, 70, 100, 70.0, row_number() OVER (ORDER BY id)::smallint FROM assessment_roles
                """, attemptId);
    }

    @Test
    void buildsFromQ1M1DataPersistsOnceAndReusesTheSuccessfulDiagnosis() {
        var first = service.generateInitial(userId, attemptId, ROLE_ID);
        var persisted = diagnostics.findByAttemptIdAndRoleId(attemptId, ROLE_ID).orElseThrow();
        var second = service.generateInitial(userId, attemptId, ROLE_ID);

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, first.status());
        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, second.status());
        assertEquals("QA", persisted.roleNameSnapshot);
        assertEquals(1, persisted.attemptCount);
        assertNotNull(persisted.contextHash);
        assertEquals(64, persisted.contextHash.trim().length());
        assertEquals(70.0, fake.lastContext().matchingPercentage().doubleValue());
        assertFalse(fake.lastContext().competencies().isEmpty());
        assertTrue(fake.lastContext().competencies().stream().allMatch(c -> c.gap() >= 0));
        assertEquals(1, fake.invocationCount());
    }

    @Test
    void buildsQ2ContextFromTheAttemptPinnedQuestionnaireWithoutUsingQ1Text() {
        UUID q2UserId = UUID.randomUUID();
        UUID q2AttemptId = UUID.randomUUID();
        jdbc.update("INSERT INTO users(id,email,first_name,last_name,password_hash,role) VALUES (?, ?, 'Fixture', 'User', 'hash', 'STUDENT')",
                q2UserId, "explainability-q2-" + q2UserId + "@example.test");
        jdbc.update("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, 'COMPLETED', ?, ?, now(), now())
                """, q2AttemptId, q2UserId, Q2, M1);
        jdbc.update("""
                INSERT INTO assessment_answers(attempt_id,questionnaire_version_id,competency_code,level)
                SELECT ?, ?, competency_code, 1 FROM assessment_questions WHERE questionnaire_version_id=?
                """, q2AttemptId, Q2, Q2);
        jdbc.update("INSERT INTO assessment_result_sets(attempt_id,algorithm_version) VALUES (?, 'matching-v2.0')",
                q2AttemptId);
        jdbc.update("""
                INSERT INTO assessment_role_results(result_set_id,role_id,score_numerator,score_denominator,display_percentage,rank_position)
                SELECT ?, id, 555, 1000, 55.5, row_number() OVER (ORDER BY id)::smallint FROM assessment_roles
                """, q2AttemptId);

        var result = service.generateInitial(q2UserId, q2AttemptId, "backend-dev-jr");
        var apiCompetency = fake.lastContext().competencies().stream()
                .filter(competency -> competency.id().equals("rest-api-consumption")).findFirst().orElseThrow();

        assertEquals(ExplainabilityDiagnosticStatus.SUCCEEDED, result.status());
        assertEquals(55.5, fake.lastContext().matchingPercentage().doubleValue());
        assertEquals("Comprendo cómo una aplicación realiza solicitudes HTTP a una API REST existente y utiliza las respuestas obtenidas.",
                apiCompetency.current().description());
        assertEquals(1, fake.invocationCount());
    }

    @TestConfiguration
    static class FakeProviderConfiguration {
        @Bean
        @Primary
        FakeDiagnosisGenerator fakeDiagnosisGenerator() { return new FakeDiagnosisGenerator(); }
    }
}
