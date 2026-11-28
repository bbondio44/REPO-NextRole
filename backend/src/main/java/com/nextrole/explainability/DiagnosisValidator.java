package com.nextrole.explainability;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DiagnosisValidator {
    private static final Set<String> REQUIRED_FIELDS = Set.of(
            "currentSituation", "recommendedFocus", "importanceExplanation",
            "professionalRelevance", "highlightedCompetencyIds");

    private final ObjectMapper mapper;

    public DiagnosisValidator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ValidatedDiagnosis validate(String rawJson, Set<String> allowedCompetencyIds) {
        if (rawJson == null || rawJson.isBlank()) throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_JSON);
        final JsonNode root;
        try (JsonParser parser = mapper.getFactory().createParser(rawJson)) {
            parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
            root = mapper.readTree(parser);
            if (parser.nextToken() != null) throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_JSON);
        } catch (ValidationException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_JSON);
        } catch (Exception e) {
            throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_JSON);
        }
        if (root == null || !root.isObject()) throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_SCHEMA);
        Set<String> actualFields = new HashSet<>();
        root.fieldNames().forEachRemaining(actualFields::add);
        if (!actualFields.equals(REQUIRED_FIELDS)) throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_SCHEMA);

        String current = text(root, "currentSituation");
        String focus = text(root, "recommendedFocus");
        String importance = text(root, "importanceExplanation");
        String relevance = text(root, "professionalRelevance");
        JsonNode idsNode = root.get("highlightedCompetencyIds");
        if (!idsNode.isArray()) throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_SCHEMA);
        List<String> ids = new ArrayList<>(idsNode.size());
        Set<String> seen = new HashSet<>();
        for (JsonNode idNode : idsNode) {
            if (!idNode.isTextual() || idNode.textValue().isBlank()) throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_SCHEMA);
            String id = idNode.textValue();
            if (!seen.add(id) || !allowedCompetencyIds.contains(id)) {
                throw invalid(DiagnosisGenerator.FailureCode.INVALID_COMPETENCY_REFERENCE);
            }
            ids.add(id);
        }
        return new ValidatedDiagnosis(current, focus, importance, relevance, List.copyOf(ids));
    }

    private static String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (!value.isTextual() || value.textValue().isBlank()) {
            throw invalid(DiagnosisGenerator.FailureCode.INVALID_DIAGNOSIS_SCHEMA);
        }
        return value.textValue();
    }

    private static ValidationException invalid(DiagnosisGenerator.FailureCode code) {
        return new ValidationException(code);
    }

    public static final class ValidationException extends IllegalArgumentException {
        private final DiagnosisGenerator.FailureCode failureCode;

        private ValidationException(DiagnosisGenerator.FailureCode failureCode) {
            super(failureCode.name());
            this.failureCode = failureCode;
        }

        public DiagnosisGenerator.FailureCode failureCode() { return failureCode; }
    }

    @JsonPropertyOrder({"currentSituation", "recommendedFocus", "importanceExplanation",
            "professionalRelevance", "highlightedCompetencyIds"})
    public record ValidatedDiagnosis(String currentSituation, String recommendedFocus,
                                     String importanceExplanation, String professionalRelevance,
                                     List<String> highlightedCompetencyIds) {
        public ValidatedDiagnosis {
            highlightedCompetencyIds = List.copyOf(highlightedCompetencyIds);
        }

        public String toJson(ObjectMapper mapper) {
            try {
                return mapper.writeValueAsString(this);
            } catch (Exception e) {
                throw new IllegalStateException("Validated diagnosis could not be serialized", e);
            }
        }
    }
}
