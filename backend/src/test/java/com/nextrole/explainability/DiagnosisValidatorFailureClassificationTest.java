package com.nextrole.explainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosisValidatorFailureClassificationTest {
    private final DiagnosisValidator validator = new DiagnosisValidator(new ObjectMapper());
    private final Set<String> allowedIds = Set.of("api-testing");

    @Test
    void classifiesMalformedJsonSeparatelyFromSchemaViolationsAndUnknownCompetencies() {
        assertCode("INVALID_DIAGNOSIS_JSON", "{");
        assertCode("INVALID_DIAGNOSIS_JSON", "```json\n" + diagnosis("api-testing") + "\n```");
        assertCode("INVALID_DIAGNOSIS_SCHEMA", "{}");
        assertCode("INVALID_DIAGNOSIS_SCHEMA", diagnosis("api-testing").replace("\"highlightedCompetencyIds\"", "\"additional\":true,\"highlightedCompetencyIds\""));
        assertCode("INVALID_COMPETENCY_REFERENCE", diagnosis("unknown-id"));
    }

    private void assertCode(String expected, String response) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(response, allowedIds));
        assertEquals(expected, error.getMessage());
    }

    private static String diagnosis(String highlightedId) {
        return "{\"currentSituation\":\"Actual\",\"recommendedFocus\":\"Foco\","
                + "\"importanceExplanation\":\"Importancia\",\"professionalRelevance\":\"Rol\","
                + "\"highlightedCompetencyIds\":[\"" + highlightedId + "\"]}";
    }
}
