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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            assertQuestionnaireVersionHistory(connection);
            assertMatrixMatches(connection, matrix);
            assertRolesExist(connection);
        }
    }

    private static void assertQuestionnaireVersionHistory(Connection connection) throws SQLException {
        Map<String, Boolean> versions = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT version_code, is_current FROM assessment_questionnaire_versions ORDER BY version_code");
                ResultSet rows = statement.executeQuery()) {
            while (rows.next()) versions.put(rows.getString("version_code"), rows.getBoolean("is_current"));
        }
        assertEquals(Map.of("Q1", false, "Q2", true), versions);

        Map<String, String> q1Questions = loadQuestionSnapshot(connection, "Q1");
        Map<String, String> q2Questions = loadQuestionSnapshot(connection, "Q2");
        assertEquals(31, q1Questions.size());
        assertEquals(q1Questions, q2Questions, "Q2 question IDs, wording and order must match Q1");

        Map<String, String> q1Options = loadOptionSnapshot(connection, "Q1");
        Map<String, String> q2Options = loadOptionSnapshot(connection, "Q2");
        assertEquals(155, q1Options.size());
        assertEquals(q1Options.keySet(), q2Options.keySet());
        Map<String, String> approved = approvedQ2Descriptions();
        Set<String> changed = new HashSet<>();
        for (String key : q1Options.keySet()) {
            if (!q1Options.get(key).equals(q2Options.get(key))) changed.add(key);
            if (!approved.containsKey(key)) {
                assertEquals(q1Options.get(key), q2Options.get(key), "unchanged option " + key);
            }
        }
        assertEquals(approved.keySet(), changed);
        approved.forEach((key, description) -> assertEquals(description, q2Options.get(key), key));
        assertEquals(186, scalarInt(connection,
                "SELECT count(*) FROM assessment_role_requirements r JOIN assessment_matrix_versions m " +
                "ON m.id = r.matrix_version_id WHERE m.version_code = 'M1' AND m.is_current = TRUE"));
        assertEquals(1, scalarInt(connection,
                "SELECT count(*) FROM assessment_matrix_versions WHERE is_current = TRUE AND version_code = 'M1'"));
    }

    private static Map<String, String> loadQuestionSnapshot(Connection connection, String version) throws SQLException {
        String sql = """
                SELECT q.competency_code, q.position, q.title, q.question_text
                  FROM assessment_questions q
                  JOIN assessment_questionnaire_versions v ON v.id = q.questionnaire_version_id
                 WHERE v.version_code = ? ORDER BY q.position
                """;
        Map<String, String> snapshot = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, version);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) snapshot.put(rows.getString("competency_code"),
                        rows.getInt("position") + "|" + rows.getString("title") + "|" + rows.getString("question_text"));
            }
        }
        return snapshot;
    }

    private static Map<String, String> loadOptionSnapshot(Connection connection, String version) throws SQLException {
        String sql = """
                SELECT o.competency_code, o.level, o.description
                  FROM assessment_answer_options o
                  JOIN assessment_questionnaire_versions v ON v.id = o.questionnaire_version_id
                 WHERE v.version_code = ? ORDER BY o.competency_code, o.level
                """;
        Map<String, String> snapshot = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, version);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) snapshot.put(optionKey(rows.getString("competency_code"), rows.getInt("level")),
                        rows.getString("description"));
            }
        }
        return snapshot;
    }

    private static Map<String, String> approvedQ2Descriptions() {
        return Map.ofEntries(
                Map.entry(optionKey("rest-api-consumption", 0), "No comprendo cómo se realizan solicitudes a una API REST existente ni cómo se utiliza la información que devuelve."),
                Map.entry(optionKey("rest-api-consumption", 1), "Comprendo cómo una aplicación realiza solicitudes HTTP a una API REST existente y utiliza las respuestas obtenidas."),
                Map.entry(optionKey("rest-api-development", 1), "Comprendo cómo funciona una API REST desde la perspectiva de su desarrollo y qué función cumplen los endpoints, los métodos HTTP y las respuestas JSON."),
                Map.entry(optionKey("api-testing", 1), "Comprendo cómo se verifica el comportamiento de una API y qué aspectos se deben comprobar al probar sus endpoints."),
                Map.entry(optionKey("api-testing", 2), "Puedo enviar solicitudes sencillas a una API utilizando herramientas como Postman y comprobar si la respuesta coincide con un resultado esperado simple."),
                Map.entry(optionKey("programming-fundamentals", 3), "Tengo experiencia desarrollando programas para resolver problemas habituales, organizando la lógica en funciones y verificando que los resultados obtenidos sean los esperados."),
                Map.entry(optionKey("programming-fundamentals", 4), "Puedo comparar diferentes formas de resolver un problema, evaluar la lógica y organización del código, y justificar las decisiones de implementación tomadas."),
                Map.entry(optionKey("object-oriented-programming", 4), "Puedo evaluar el diseño de clases y objetos, identificar problemas en la distribución de responsabilidades, cohesión o acoplamiento, y mejorar el modelo utilizando abstracciones y relaciones adecuadas."),
                Map.entry(optionKey("web-interface-development", 3), "Tengo experiencia desarrollando y adaptando interfaces con formularios, componentes reutilizables e interacciones, verificando su funcionamiento y visualización en diferentes tamaños de pantalla."),
                Map.entry(optionKey("data-cleaning-preparation", 2), "Puedo identificar problemas sencillos, como duplicados, valores faltantes o formatos incorrectos, y aplicar un tratamiento apropiado con herramientas de análisis de datos."));
    }

    private static String optionKey(String competency, int level) {
        return competency + ":" + level;
    }

    private static int scalarInt(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rows = statement.executeQuery()) {
            assertTrue(rows.next());
            return rows.getInt(1);
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
