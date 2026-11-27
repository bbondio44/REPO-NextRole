package com.nextrole.explainability;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonPropertyOrder({"id", "name", "current", "required", "classification", "gap", "trainingPriority"})
public record ExplainabilityCompetencyContext(
        String id,
        String name,
        LevelContext current,
        LevelContext required,
        Classification classification,
        int gap,
        int trainingPriority) {

    public ExplainabilityCompetencyContext {
        requireText(id, "id");
        requireText(name, "name");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(required, "required");
        Objects.requireNonNull(classification, "classification");
        int expectedGap = Math.max(0, required.level() - current.level());
        int expectedPriority = expectedGap * classification.weight();
        if (gap != expectedGap) throw new IllegalArgumentException("gap does not match current and required levels");
        if (trainingPriority != expectedPriority) {
            throw new IllegalArgumentException("trainingPriority does not match levels and classification");
        }
    }

    public enum Classification {
        ESSENTIAL(2), COMPLEMENTARY(1);

        private final int weight;

        Classification(int weight) { this.weight = weight; }

        public int weight() { return weight; }

        public static Classification fromImportance(com.nextrole.data.AssessmentRequirementImportance importance) {
            return switch (importance) {
                case ESSENTIAL -> ESSENTIAL;
                case COMPLEMENTARY -> COMPLEMENTARY;
                case NOT_RELEVANT -> throw new IllegalArgumentException("Non-relevant competencies are not sent");
            };
        }
    }

    @JsonPropertyOrder({"level", "description"})
    public record LevelContext(int level, String description) {
        public LevelContext {
            if (level < 0 || level > 4) throw new IllegalArgumentException("level must be between 0 and 4");
            requireText(description, "description");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    }
}
