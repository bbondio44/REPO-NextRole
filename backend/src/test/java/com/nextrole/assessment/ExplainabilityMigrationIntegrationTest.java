package com.nextrole.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ExplainabilityMigrationIntegrationTest {
    private static final UUID USER = UUID.fromString("71000000-0000-0000-0000-000000000001");
    private static final UUID Q1_ATTEMPT = UUID.fromString("72000000-0000-0000-0000-000000000001");
    private static final UUID Q2_ATTEMPT = UUID.fromString("72000000-0000-0000-0000-000000000002");
    private static final UUID Q1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID M1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID Q2 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void v7AddsRoleCatalogAndDiagnosticConstraintsWithoutChangingHistoricalAssessmentData() throws Exception {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .target("6").locations("classpath:db/migration").load().migrate();
        try (Connection connection = connection()) {
            seedHistoricalData(connection);
        }
        List<String> before = historicalSnapshot();

        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration").load().migrate();

        try (Connection connection = connection()) {
            assertEquals(before, historicalSnapshot(connection), "V7 must preserve Q1/Q2 attempts, answers and Matching");
            assertEquals(6, scalar(connection, "SELECT count(*) FROM assessment_roles WHERE description IS NOT NULL"));
            assertEquals(0, scalar(connection, "SELECT count(*) FROM explainability_diagnostics"));
            assertEquals(31, scalar(connection, "SELECT count(*) FROM assessment_answers WHERE attempt_id='" + Q1_ATTEMPT + "'"));
            assertEquals(31, scalar(connection, "SELECT count(*) FROM assessment_answers WHERE attempt_id='" + Q2_ATTEMPT + "'"));
            assertEquals(12, scalar(connection, "SELECT count(*) FROM assessment_role_results"));

            assertConstraintRejects(connection, "INSERT INTO explainability_diagnostics(attempt_id, role_id, status) VALUES ('" + Q1_ATTEMPT + "', 'qa-jr', 'UNKNOWN')");
            assertConstraintRejects(connection, "INSERT INTO explainability_diagnostics(attempt_id, role_id, status) VALUES ('" + Q1_ATTEMPT + "', 'qa-jr', 'SUCCEEDED')");
            assertConstraintRejects(connection, "INSERT INTO explainability_diagnostics(attempt_id, role_id, status, generation_token, lease_until) VALUES ('" + Q1_ATTEMPT + "', 'qa-jr', 'GENERATING', NULL, now())");

            UUID row = UUID.randomUUID();
            insertGenerating(connection, row, Q1_ATTEMPT, "qa-jr");
            assertConstraintRejects(connection, "INSERT INTO explainability_diagnostics(attempt_id, role_id, status, generation_token, lease_until) VALUES ('" + Q1_ATTEMPT + "', 'qa-jr', 'GENERATING', gen_random_uuid(), now())");
            try (PreparedStatement success = connection.prepareStatement("""
                    UPDATE explainability_diagnostics
                       SET status='SUCCEEDED', diagnosis_content='{}'::jsonb,
                           role_name_snapshot='QA', role_description_snapshot='QA description',
                           provider='openrouter', model_requested='model', prompt_version='explainability-v5',
                           prompt_hash=repeat('a',64), context_hash=repeat('b',64), succeeded_at=now(), lease_until=NULL
                     WHERE id=?
                    """)) {
                success.setObject(1, row);
                assertEquals(1, success.executeUpdate());
            }
        }
    }

    private static void seedHistoricalData(Connection connection) throws SQLException {
        try (PreparedStatement user = connection.prepareStatement(
                "INSERT INTO users(id,email,first_name,last_name,password_hash,role) VALUES (?, ?, 'Test', 'User', 'hash', 'STUDENT')")) {
            user.setObject(1, USER);
            user.setString(2, "explainability-migration@example.test");
            user.executeUpdate();
        }
        insertAttempt(connection, Q1_ATTEMPT, Q1);
        insertAttempt(connection, Q2_ATTEMPT, Q2);
        for (UUID attemptId : List.of(Q1_ATTEMPT, Q2_ATTEMPT)) {
            try (PreparedStatement answers = connection.prepareStatement("""
                    INSERT INTO assessment_answers(attempt_id, questionnaire_version_id, competency_code, level)
                    SELECT ?, ?, competency_code, 2 FROM assessment_questions WHERE questionnaire_version_id=?
                    """)) {
                answers.setObject(1, attemptId);
                answers.setObject(2, attemptId.equals(Q1_ATTEMPT) ? Q1 : Q2);
                answers.setObject(3, attemptId.equals(Q1_ATTEMPT) ? Q1 : Q2);
                assertEquals(31, answers.executeUpdate());
            }
            try (PreparedStatement resultSet = connection.prepareStatement(
                    "INSERT INTO assessment_result_sets(attempt_id,algorithm_version) VALUES (?, 'matching-v2.0')")) {
                resultSet.setObject(1, attemptId);
                resultSet.executeUpdate();
            }
            try (PreparedStatement results = connection.prepareStatement("""
                    INSERT INTO assessment_role_results(result_set_id,role_id,score_numerator,score_denominator,display_percentage,rank_position)
                    SELECT ?, id, 42, 60, 70.0, row_number() OVER (ORDER BY id)::smallint FROM assessment_roles
                    """)) {
                results.setObject(1, attemptId);
                assertEquals(6, results.executeUpdate());
            }
        }
    }

    private static void insertAttempt(Connection connection, UUID id, UUID questionnaire) throws SQLException {
        try (PreparedStatement attempt = connection.prepareStatement("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, 'COMPLETED', ?, ?, now(), now())
                """)) {
            attempt.setObject(1, id);
            attempt.setObject(2, USER);
            attempt.setObject(3, questionnaire);
            attempt.setObject(4, M1);
            attempt.executeUpdate();
        }
    }

    private static List<String> historicalSnapshot() throws SQLException {
        try (Connection connection = connection()) {
            return historicalSnapshot(connection);
        }
    }

    private static List<String> historicalSnapshot(Connection connection) throws SQLException {
        List<String> snapshot = new ArrayList<>();
        for (String table : List.of("assessment_attempts", "assessment_answers", "assessment_result_sets",
                "assessment_role_results", "assessment_questionnaire_versions", "assessment_matrix_versions",
                "assessment_questions", "assessment_answer_options", "assessment_role_requirements", "roles")) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " ORDER BY 1");
                    ResultSet rows = statement.executeQuery()) {
                var metadata = rows.getMetaData();
                while (rows.next()) {
                    StringBuilder row = new StringBuilder(table);
                    for (int index = 1; index <= metadata.getColumnCount(); index++) {
                        row.append('|').append(rows.getString(index));
                    }
                    snapshot.add(row.toString());
                }
            }
        }
        snapshot.sort(String::compareTo);
        return snapshot;
    }

    private static void insertGenerating(Connection connection, UUID id, UUID attempt, String role) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO explainability_diagnostics(id,attempt_id,role_id,status,generation_token,lease_until,attempt_count,created_at,updated_at)
                VALUES (?, ?, ?, 'GENERATING', ?, now() + interval '2 minutes', 1, now(), now())
                """)) {
            insert.setObject(1, id);
            insert.setObject(2, attempt);
            insert.setString(3, role);
            insert.setObject(4, UUID.randomUUID());
            insert.executeUpdate();
        }
    }

    private static void assertConstraintRejects(Connection connection, String sql) throws SQLException {
        connection.setAutoCommit(false);
        assertThrows(SQLException.class, () -> {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        });
        connection.rollback();
        connection.setAutoCommit(true);
    }

    private static int scalar(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
