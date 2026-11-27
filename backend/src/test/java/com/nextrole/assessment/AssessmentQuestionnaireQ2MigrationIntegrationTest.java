package com.nextrole.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AssessmentQuestionnaireQ2MigrationIntegrationTest {
    private static final UUID Q1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID M1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID USER = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID DRAFT = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID COMPLETE = UUID.fromString("40000000-0000-0000-0000-000000000002");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void q2UpgradePreservesPinnedQ1DraftCompletedAnswersResultsAndM1() throws Exception {
        migrateThroughV5();
        Map<String, String> q1OptionsBefore;
        List<String> matrixBefore;
        Map<String, String> completedAnswersBefore;
        List<String> completedResultsBefore;
        try (Connection connection = connection()) {
            seedHistoricalAttempts(connection);
            q1OptionsBefore = optionSnapshot(connection, "Q1");
            matrixBefore = matrixSnapshot(connection);
            completedAnswersBefore = answerSnapshot(connection, COMPLETE);
            completedResultsBefore = resultSnapshot(connection, COMPLETE);
        }

        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration").load().migrate();

        try (Connection connection = connection()) {
            assertEquals(Map.of("Q1", false, "Q2", true), questionnaireVersions(connection));
            assertEquals(q1OptionsBefore, optionSnapshot(connection, "Q1"), "Q1 option descriptions must remain immutable");
            assertEquals(matrixBefore, matrixSnapshot(connection), "M1 requirements must remain unchanged");
            assertEquals(completedAnswersBefore, answerSnapshot(connection, COMPLETE));
            assertEquals(completedResultsBefore, resultSnapshot(connection, COMPLETE));
            assertEquals(31, scalar(connection, "SELECT count(*) FROM assessment_answers WHERE attempt_id = '" + COMPLETE + "'"));
            assertEquals(2, scalar(connection, "SELECT count(*) FROM assessment_answers WHERE attempt_id = '" + DRAFT + "'"));

            assertAttempt(connection, DRAFT, "DRAFT", "Q1", "M1");
            assertAttempt(connection, COMPLETE, "COMPLETED", "Q1", "M1");
            assertEquals(6, scalar(connection, "SELECT count(*) FROM assessment_role_results WHERE result_set_id = '" + COMPLETE + "'"));

            Map<String, String> q2Options = optionSnapshot(connection, "Q2");
            assertEquals(155, q1OptionsBefore.size());
            assertEquals(q1OptionsBefore.keySet(), q2Options.keySet());
            long differences = q1OptionsBefore.keySet().stream()
                    .filter(key -> !q1OptionsBefore.get(key).equals(q2Options.get(key))).count();
            assertEquals(10, differences);
            assertEquals(31, scalar(connection, "SELECT count(*) FROM assessment_questions WHERE questionnaire_version_id = '00000000-0000-0000-0000-000000000003'"));
            assertEquals(155, q2Options.size());
            assertEquals(186, matrixBefore.size());
        }
    }

    private static void migrateThroughV5() {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration").target(MigrationVersion.fromVersion("5")).load().migrate();
    }

    private static void seedHistoricalAttempts(Connection connection) throws SQLException {
        try (PreparedStatement user = connection.prepareStatement(
                "INSERT INTO users(id,email,first_name,last_name,password_hash,role) VALUES (?, ?, 'Test', 'Student', 'test-hash', 'STUDENT')")) {
            user.setObject(1, USER);
            user.setString(2, "q2-upgrade@example.test");
            user.executeUpdate();
        }
        try (PreparedStatement attempt = connection.prepareStatement("""
                INSERT INTO assessment_attempts(id,user_id,status,questionnaire_version_id,matrix_version_id,created_at,completed_at)
                VALUES (?, ?, ?, ?, ?, now(), CASE WHEN ? = 'COMPLETED' THEN now() ELSE NULL END)
                """)) {
            insertAttempt(attempt, DRAFT, "DRAFT");
            insertAttempt(attempt, COMPLETE, "COMPLETED");
        }
        try (PreparedStatement answers = connection.prepareStatement("""
                INSERT INTO assessment_answers(attempt_id,questionnaire_version_id,competency_code,level)
                SELECT ?, ?, q.competency_code, (q.position % 5)::smallint
                  FROM assessment_questions q
                 WHERE q.questionnaire_version_id = ? AND (? OR q.position <= 2)
                 ORDER BY q.position
                """)) {
            answers.setObject(1, DRAFT);
            answers.setObject(2, Q1);
            answers.setObject(3, Q1);
            answers.setBoolean(4, false);
            assertEquals(2, answers.executeUpdate());
            answers.setObject(1, COMPLETE);
            answers.setObject(2, Q1);
            answers.setObject(3, Q1);
            answers.setBoolean(4, true);
            assertEquals(31, answers.executeUpdate());
        }
        try (PreparedStatement resultSet = connection.prepareStatement(
                "INSERT INTO assessment_result_sets(attempt_id,algorithm_version) VALUES (?, 'matching-v2.0')")) {
            resultSet.setObject(1, COMPLETE);
            resultSet.executeUpdate();
        }
        try (PreparedStatement results = connection.prepareStatement("""
                INSERT INTO assessment_role_results(result_set_id,role_id,score_numerator,score_denominator,display_percentage,rank_position)
                SELECT ?, id, 20, 1, 20.0, row_number() OVER (ORDER BY id)::smallint FROM assessment_roles
                """)) {
            results.setObject(1, COMPLETE);
            assertEquals(6, results.executeUpdate());
        }
    }

    private static void insertAttempt(PreparedStatement statement, UUID id, String status) throws SQLException {
        statement.setObject(1, id);
        statement.setObject(2, USER);
        statement.setString(3, status);
        statement.setObject(4, Q1);
        statement.setObject(5, M1);
        statement.setString(6, status);
        statement.executeUpdate();
    }

    private static Map<String, Boolean> questionnaireVersions(Connection connection) throws SQLException {
        Map<String, Boolean> result = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT version_code,is_current FROM assessment_questionnaire_versions ORDER BY version_code");
                ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.put(rows.getString(1), rows.getBoolean(2));
        }
        return result;
    }

    private static Map<String, String> optionSnapshot(Connection connection, String version) throws SQLException {
        Map<String, String> result = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT o.competency_code,o.level,o.description FROM assessment_answer_options o
                JOIN assessment_questionnaire_versions v ON v.id=o.questionnaire_version_id WHERE v.version_code=?
                """)) {
            statement.setString(1, version);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.put(rows.getString(1) + ":" + rows.getInt(2), rows.getString(3));
            }
        }
        return result;
    }

    private static List<String> matrixSnapshot(Connection connection) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT r.role_id,r.competency_code,r.importance,r.expected_level
                  FROM assessment_role_requirements r JOIN assessment_matrix_versions m ON m.id=r.matrix_version_id
                 WHERE m.version_code='M1' ORDER BY r.role_id,r.competency_code
                """); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(rows.getString(1) + "|" + rows.getString(2) + "|" + rows.getString(3) + "|" + rows.getString(4));
        }
        return result;
    }

    private static Map<String, String> answerSnapshot(Connection connection, UUID attempt) throws SQLException {
        Map<String, String> result = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT competency_code,level,questionnaire_version_id FROM assessment_answers WHERE attempt_id=?")) {
            statement.setObject(1, attempt);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.put(rows.getString(1), rows.getInt(2) + "|" + rows.getString(3));
            }
        }
        return result;
    }

    private static List<String> resultSnapshot(Connection connection, UUID attempt) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT role_id,score_numerator,score_denominator,display_percentage,rank_position
                  FROM assessment_role_results WHERE result_set_id=? ORDER BY rank_position
                """)) {
            statement.setObject(1, attempt);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(rows.getString(1) + "|" + rows.getString(2) + "|" + rows.getString(3) + "|" + rows.getString(4) + "|" + rows.getString(5));
            }
        }
        return result;
    }

    private static void assertAttempt(Connection connection, UUID id, String status, String questionnaire, String matrix)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT a.status,q.version_code,m.version_code FROM assessment_attempts a
                JOIN assessment_questionnaire_versions q ON q.id=a.questionnaire_version_id
                JOIN assessment_matrix_versions m ON m.id=a.matrix_version_id WHERE a.id=?
                """)) {
            statement.setObject(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                assertEquals(status, rows.getString(1));
                assertEquals(questionnaire, rows.getString(2));
                assertEquals(matrix, rows.getString(3));
                assertFalse(rows.next());
            }
        }
    }

    private static int scalar(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
