package com.nextrole.assessment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AssessmentMigrationIntegrationTest {
    private static final String CANONICAL_CATALOG = "docs/V2/autoevaluacion-matriz-vigente.md";
    private static final List<String> ROLE_IDS = List.of(
            "backend-dev-jr", "frontend-dev-jr", "fullstack-dev-jr", "qa-jr", "data-analyst-jr", "cloud-jr");
    private static final Map<String, String> ROLE_LABELS = Map.of(
            "backend-dev-jr", "Backend",
            "frontend-dev-jr", "Frontend",
            "fullstack-dev-jr", "Full Stack",
            "qa-jr", "QA",
            "data-analyst-jr", "Data Analyst",
            "cloud-jr", "Cloud");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void migrateDisposableDatabase() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @Test
    void migrationSeedsEveryCanonicalQuestionOptionAndMatrixRequirement() throws Exception {
        String markdown = Files.readString(findRepositoryFile(CANONICAL_CATALOG));
        List<Question> questions = parseQuestions(markdown);
        List<MatrixRow> matrix = parseMatrix(markdown);

        assertEquals(31, questions.size(), "canonical question count");
        assertEquals(31, matrix.size(), "canonical matrix row count");
        assertEquals(155, questions.stream().mapToInt(question -> question.options().size()).sum(),
                "canonical answer option count");
        assertEquals(186, matrix.size() * ROLE_IDS.size(), "canonical matrix cell count");

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertQuestionnaireMatches(connection, questions);
            assertMatrixMatches(connection, matrix);
            assertRolesExist(connection);
        }
    }

    private static void assertQuestionnaireMatches(Connection connection, List<Question> expected)
            throws SQLException {
        String sql = """
                SELECT q.position, q.title, q.question_text, o.level, o.description
                  FROM assessment_questionnaire_versions v
                  JOIN assessment_questions q ON q.questionnaire_version_id = v.id
                  JOIN assessment_answer_options o
                    ON o.questionnaire_version_id = q.questionnaire_version_id
                   AND o.competency_code = q.competency_code
                 WHERE v.is_current = TRUE
                 ORDER BY q.position, o.level
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            int rowCount = 0;
            for (Question question : expected) {
                for (Option option : question.options()) {
                    assertTrue(rows.next(), "missing seeded option for question " + question.position());
                    assertEquals(question.position(), rows.getInt("position"));
                    assertEquals(question.title(), rows.getString("title"));
                    assertEquals(question.prompt(), rows.getString("question_text"));
                    assertEquals(option.level(), rows.getInt("level"));
                    assertEquals(option.description(), rows.getString("description"));
                    rowCount++;
                }
            }
            assertFalse(rows.next(), "unexpected extra seeded question or option");
            assertEquals(155, rowCount);
        }
    }

    private static void assertMatrixMatches(Connection connection, List<MatrixRow> expected)
            throws SQLException {
        String sql = """
                SELECT q.position, r.role_id, r.importance, r.expected_level
                  FROM assessment_matrix_versions v
                  JOIN assessment_role_requirements r ON r.matrix_version_id = v.id
                  JOIN assessment_questionnaire_versions qv ON qv.is_current = TRUE
                  JOIN assessment_questions q
                    ON q.questionnaire_version_id = qv.id
                   AND q.competency_code = r.competency_code
                 WHERE v.is_current = TRUE
                 ORDER BY q.position, r.role_id
                """;
        Map<Integer, MatrixRow> byPosition = new HashMap<>();
        for (MatrixRow row : expected) {
            byPosition.put(row.position(), row);
        }

        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            int cellCount = 0;
            while (rows.next()) {
                int position = rows.getInt("position");
                String roleId = rows.getString("role_id");
                MatrixRow expectedRow = byPosition.get(position);
                assertNotNull(expectedRow, "unexpected competency position " + position);
                assertTrue(ROLE_IDS.contains(roleId), "unexpected role ID " + roleId);

                Requirement expectedRequirement = expectedRow.requirements().get(roleId);
                assertNotNull(expectedRequirement, "missing canonical cell for " + position + "/" + roleId);
                assertEquals(expectedRequirement.importance(), rows.getString("importance"),
                        "importance for competency " + position + ", role " + roleId);
                int expectedLevel = rows.getInt("expected_level");
                if (expectedRequirement.expectedLevel() == null) {
                    assertTrue(rows.wasNull(), "non-relevant cell must have no expected level");
                    assertNull(expectedRequirement.expectedLevel());
                } else {
                    assertFalse(rows.wasNull(), "relevant cell must have an expected level");
                    assertEquals(expectedRequirement.expectedLevel().intValue(), expectedLevel,
                            "expected level for competency " + position + ", role " + roleId);
                }
                cellCount++;
            }
            assertEquals(186, cellCount, "seeded role-matrix cell count");
        }
    }

    private static void assertRolesExist(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name FROM assessment_roles WHERE id = ANY (?) ORDER BY id")) {
            statement.setArray(1, connection.createArrayOf("varchar", ROLE_IDS.toArray()));
            try (ResultSet rows = statement.executeQuery()) {
                Map<String, String> actual = new HashMap<>();
                while (rows.next()) {
                    actual.put(rows.getString("id"), rows.getString("name"));
                }
                assertEquals(ROLE_LABELS, actual);
            }
        }
    }

    private static List<Question> parseQuestions(String markdown) {
        String questionSection = markdown.split("(?m)^## Matriz de requisitos por rol\\s*$", 2)[0];
        Pattern blockPattern = Pattern.compile(
                "(?ms)^### Pregunta (\\d+) — (.+?)\\R(.*?)(?=^### Pregunta |\\z)");
        Pattern promptPattern = Pattern.compile("(?m)^\\*\\*Pregunta:\\*\\* (.+)$");
        Pattern optionPattern = Pattern.compile("(?m)^- \\*\\*Nivel ([0-4]):\\*\\* (.+)$");
        Matcher blocks = blockPattern.matcher(questionSection);
        List<Question> result = new ArrayList<>();
        while (blocks.find()) {
            int position = Integer.parseInt(blocks.group(1));
            Matcher promptMatcher = promptPattern.matcher(blocks.group(3));
            assertTrue(promptMatcher.find(), "question prompt missing at position " + position);
            Matcher optionMatcher = optionPattern.matcher(blocks.group(3));
            List<Option> options = new ArrayList<>();
            while (optionMatcher.find()) {
                options.add(new Option(Integer.parseInt(optionMatcher.group(1)), optionMatcher.group(2)));
            }
            assertEquals(List.of(0, 1, 2, 3, 4), options.stream().map(Option::level).toList(),
                    "level options for question " + position);
            result.add(new Question(position, blocks.group(2).trim(), promptMatcher.group(1), options));
        }
        return result;
    }

    private static List<MatrixRow> parseMatrix(String markdown) {
        Matcher rows = Pattern.compile("(?m)^\\|\\s*(\\d+)\\s*\\|([^\\r\\n]+)$").matcher(markdown);
        List<MatrixRow> result = new ArrayList<>();
        while (rows.find()) {
            String[] cells = rows.group(2).split("\\|");
            if (cells.length != 7) {
                continue;
            }
            int position = Integer.parseInt(rows.group(1));
            Map<String, Requirement> requirements = new HashMap<>();
            for (int index = 0; index < ROLE_IDS.size(); index++) {
                String cell = cells[index + 1].trim();
                Matcher requirement = Pattern.compile("([EC])([1-4])").matcher(cell);
                if (requirement.matches()) {
                    requirements.put(ROLE_IDS.get(index), new Requirement(
                            requirement.group(1).equals("E") ? "ESSENTIAL" : "COMPLEMENTARY",
                            Integer.valueOf(requirement.group(2))));
                } else {
                    assertEquals("—", cell, "unknown matrix value at competency " + position);
                    requirements.put(ROLE_IDS.get(index), new Requirement("NOT_RELEVANT", null));
                }
            }
            result.add(new MatrixRow(position, requirements));
        }
        return result;
    }

    private static Path findRepositoryFile(String relativePath) throws IOException {
        Path candidate = Path.of("").toAbsolutePath();
        while (candidate != null) {
            Path file = candidate.resolve(relativePath);
            if (Files.isRegularFile(file)) {
                return file;
            }
            candidate = candidate.getParent();
        }
        throw new IOException("Could not find canonical source " + relativePath + " above "
                + Path.of("").toAbsolutePath());
    }

    private record Question(int position, String title, String prompt, List<Option> options) {}
    private record Option(int level, String description) {}
    private record MatrixRow(int position, Map<String, Requirement> requirements) {}
    private record Requirement(String importance, Integer expectedLevel) {}
}
