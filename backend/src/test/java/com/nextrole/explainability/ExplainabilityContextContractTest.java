package com.nextrole.explainability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExplainabilityContextContractTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesAContextCompatibleWithThePythonContractAndExcludesLocalMetadata() throws IOException {
        ExplainabilityContext context = new ExplainabilityContext(
                "data-analyst-jr", "Data Analyst",
                "Se encarga de transformar datos en información útil para la toma de decisiones. Sus responsabilidades incluyen obtener, limpiar, procesar y analizar datos, identificar patrones e interpretar indicadores de negocio. Utiliza técnicas estadísticas y herramientas de visualización para comunicar resultados, fundamentar conclusiones y contribuir a la resolución de problemas.",
                new BigDecimal("84.0"), List.of(new ExplainabilityCompetencyContext(
                "sql-queries", "Consultas SQL",
                new ExplainabilityCompetencyContext.LevelContext(2,
                        "Puedo realizar consultas sencillas para obtener, filtrar o modificar información y comprender los resultados obtenidos."),
                new ExplainabilityCompetencyContext.LevelContext(3,
                        "Tengo experiencia utilizando SQL para resolver necesidades de una aplicación, relacionar información de distintas tablas y verificar que las consultas produzcan los resultados esperados."),
                ExplainabilityCompetencyContext.Classification.ESSENTIAL, 1, 2)));

        JsonNode actual = mapper.readTree(context.canonicalJson(mapper));
        JsonNode expected = mapper.readTree(Files.readString(Path.of(
                "src/test/resources/contracts/explainability-v5-context/data-analyst-sql.json")));

        assertEquals(expected, actual);
        assertEquals(List.of("roleId", "roleName", "roleDescription", "matchingPercentage", "competencies"),
                fieldNames(actual));
        assertTrue(actual.get("matchingPercentage").isNumber());
        assertEquals(List.of("id", "name", "current", "required", "classification", "gap", "trainingPriority"),
                fieldNames(actual.get("competencies").get(0)));
        assertTrue(actual.get("competencies").get(0).get("classification").isTextual());
        assertTrue(actual.get("competencies").get(0).get("gap").isIntegralNumber());
        assertTrue(actual.get("competencies").get(0).get("trainingPriority").isIntegralNumber());
        assertFalse(actual.has("attemptId"));
        assertFalse(actual.has("userId"));
        assertFalse(actual.has("questionnaireVersion"));
        assertFalse(actual.has("matrixVersion"));
        assertFalse(actual.has("fixtureId"));
        String hash = context.sha256(mapper);
        assertTrue(hash.matches("[A-F0-9]{64}"));
        assertEquals(hash, context.sha256(mapper));
    }

    private static List<String> fieldNames(JsonNode node) {
        var names = new java.util.ArrayList<String>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
