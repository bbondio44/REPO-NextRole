package com.nextrole.explainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisValidatorTest {
    private final DiagnosisValidator validator = new DiagnosisValidator(new ObjectMapper());
    private final Set<String> allowedIds = Set.of("sql-queries", "applied-statistics");

    @Test
    void acceptsTheExactContractAndKeepsTheOriginalMeaningfulText() {
        var diagnosis = validator.validate(valid("sql-queries"), allowedIds);

        assertEquals("Situación actual", diagnosis.currentSituation());
        assertEquals("Enfoque", diagnosis.recommendedFocus());
        assertEquals("Importancia", diagnosis.importanceExplanation());
        assertEquals("Relevancia", diagnosis.professionalRelevance());
        assertEquals(java.util.List.of("sql-queries"), diagnosis.highlightedCompetencyIds());
    }

    @Test
    void rejectsMalformedMissingAdditionalBlankWrongTypeDuplicateAndUnknownData() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> validator.validate("{", allowedIds)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(valid("sql-queries").replace("\"currentSituation\":\"Situación actual\"",
                                "\"currentSituation\":\"Situación actual\",\"currentSituation\":\"Otro texto\""), allowedIds)),
                () -> assertThrows(IllegalArgumentException.class, () -> validator.validate("{}", allowedIds)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(valid("sql-queries").replace("}", ",\"extra\":true}"), allowedIds)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(valid("sql-queries").replace("Importancia", "   "), allowedIds)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(valid("sql-queries").replace("\"Enfoque\"", "42"), allowedIds)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(valid("sql-queries").replace("[\"sql-queries\"]", "[\"sql-queries\",\"sql-queries\"]"), allowedIds)),
                () -> assertThrows(IllegalArgumentException.class, () -> validator.validate(valid("missing"), allowedIds)));
    }

    private static String valid(String id) {
        return "{\"currentSituation\":\"Situación actual\",\"recommendedFocus\":\"Enfoque\","
                + "\"importanceExplanation\":\"Importancia\",\"professionalRelevance\":\"Relevancia\","
                + "\"highlightedCompetencyIds\":[\"" + id + "\"]}";
    }
}
