package com.nextrole.explainability;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextrole.data.AssessmentRequirementImportance;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({"roleId", "roleName", "roleDescription", "matchingPercentage", "competencies"})
public record ExplainabilityContext(
        String roleId,
        String roleName,
        String roleDescription,
        BigDecimal matchingPercentage,
        List<ExplainabilityCompetencyContext> competencies) {

    public ExplainabilityContext {
        requireText(roleId, "roleId");
        requireText(roleName, "roleName");
        requireText(roleDescription, "roleDescription");
        Objects.requireNonNull(matchingPercentage, "matchingPercentage");
        if (matchingPercentage.compareTo(BigDecimal.ZERO) < 0
                || matchingPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("matchingPercentage must be between 0 and 100");
        }
        competencies = List.copyOf(Objects.requireNonNull(competencies, "competencies"));
        if (competencies.isEmpty()) throw new IllegalArgumentException("competencies must not be empty");
        long distinctIds = competencies.stream().map(ExplainabilityCompetencyContext::id).distinct().count();
        if (distinctIds != competencies.size()) throw new IllegalArgumentException("competency IDs must be unique");
    }

    public String canonicalJson(ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            throw new IllegalStateException("Could not serialize Explainability context", e);
        }
    }

    public String sha256(ObjectMapper mapper) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalJson(mapper).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    }
}
