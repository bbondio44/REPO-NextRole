package com.nextrole.matching;

import com.nextrole.data.AssessmentRequirementImportance;
import com.nextrole.data.AssessmentRoleRequirement;
import com.nextrole.data.AssessmentRoleRequirementId;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineV2Test {
    private static final UUID MATRIX = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final List<String> ROLES = List.of(
            "backend-dev-jr", "frontend-dev-jr", "fullstack-dev-jr", "qa-jr", "data-analyst-jr", "cloud-jr");

    private final MatchingEngineV2 engine = new MatchingEngineV2(new Random(42));

    @Test
    void computesExpectedLevelsOneThroughFourAndTreatsZeroAsAValidLevel() {
        List<AssessmentRoleRequirement> requirements = new ArrayList<>();
        Map<String, Integer> answers = new HashMap<>();
        for (int expected = 1; expected <= 4; expected++) {
            String code = "expected-" + expected;
            requirements.add(requirement(ROLES.get(expected - 1), code,
                    AssessmentRequirementImportance.ESSENTIAL, expected));
            answers.put(code, expected - 1);
        }
        for (int roleIndex = 4; roleIndex < ROLES.size(); roleIndex++) {
            String code = "zero-" + roleIndex;
            requirements.add(requirement(ROLES.get(roleIndex), code, AssessmentRequirementImportance.ESSENTIAL, 1));
            answers.put(code, 0);
        }

        List<MatchingEngineV2.RoleScore> scores = calculate(requirements, answers);

        assertEquals("0.0", scoreFor(scores, ROLES.get(0)).displayPercentage());
        assertEquals("50.0", scoreFor(scores, ROLES.get(1)).displayPercentage());
        assertEquals("66.7", scoreFor(scores, ROLES.get(2)).displayPercentage());
        assertEquals("75.0", scoreFor(scores, ROLES.get(3)).displayPercentage());
    }

    @Test
    void capsContributionWeightsRequirementsAndExcludesIrrelevantCompetenciesFromDenominator() {
        List<AssessmentRoleRequirement> requirements = List.of(
                requirement(ROLES.get(0), "essential", AssessmentRequirementImportance.ESSENTIAL, 2),
                requirement(ROLES.get(0), "complementary", AssessmentRequirementImportance.COMPLEMENTARY, 1),
                requirement(ROLES.get(0), "irrelevant", AssessmentRequirementImportance.NOT_RELEVANT, null));
        Map<String, Integer> answers = Map.of("essential", 4, "complementary", 4, "irrelevant", 0);

        MatchingEngineV2.RoleScore score = scoreFor(calculate(requirements, answers), ROLES.get(0));

        assertEquals(BigInteger.valueOf(100), score.scoreNumerator());
        assertEquals(BigInteger.ONE, score.scoreDenominator());
        assertEquals("100.0", score.displayPercentage());
    }

    @Test
    void essentialWeightIsTwoComplementaryWeightIsOneAndEveryRelevantCellEntersItsRoleDenominator() {
        List<AssessmentRoleRequirement> requirements = List.of(
                requirement(ROLES.get(0), "essential-half", AssessmentRequirementImportance.ESSENTIAL, 4),
                requirement(ROLES.get(0), "complementary-zero", AssessmentRequirementImportance.COMPLEMENTARY, 4));
        Map<String, Integer> answers = Map.of("essential-half", 2, "complementary-zero", 0);

        MatchingEngineV2.RoleScore score = scoreFor(calculate(requirements, answers), ROLES.get(0));

        assertEquals(BigInteger.valueOf(100), score.scoreNumerator());
        assertEquals(BigInteger.valueOf(3), score.scoreDenominator());
        assertEquals("33.3", score.displayPercentage());
    }

    @Test
    void returnsAllSixRolesAndOrdersByExactScoreWithTiesAssignedAdjacentRanks() {
        List<AssessmentRoleRequirement> requirements = new ArrayList<>();
        Map<String, Integer> answers = new HashMap<>();
        for (int i = 0; i < ROLES.size(); i++) {
            String code = "skill-" + i;
            requirements.add(requirement(ROLES.get(i), code, AssessmentRequirementImportance.ESSENTIAL, 1));
            answers.put(code, i < 2 ? 1 : 0);
        }

        List<MatchingEngineV2.RoleScore> scores = calculate(requirements, answers);

        assertEquals(6, scores.size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), scores.stream().map(MatchingEngineV2.RoleScore::rankPosition).toList());
        assertEquals(Set.of(ROLES.get(0), ROLES.get(1)),
                Set.of(scores.get(0).roleId(), scores.get(1).roleId()));
        assertEquals(scores.get(0).scoreNumerator().multiply(scores.get(1).scoreDenominator()),
                scores.get(1).scoreNumerator().multiply(scores.get(0).scoreDenominator()));
    }

    @Test
    void displayPercentageUsesHalfUpWithoutChangingTheExactFraction() {
        assertEquals("68.1", MatchingEngineV2.formatPercentage(BigInteger.valueOf(1361), BigInteger.valueOf(20)));
        assertEquals("68.0", MatchingEngineV2.formatPercentage(BigInteger.valueOf(1359), BigInteger.valueOf(20)));
    }

    @Test
    void rejectsMissingAnswersInsteadOfTreatingThemAsZero() {
        List<AssessmentRoleRequirement> requirements = List.of(
                requirement(ROLES.get(0), "unanswered", AssessmentRequirementImportance.ESSENTIAL, 2));

        assertThrows(IllegalArgumentException.class, () -> engine.calculate(ROLES, requirements, Map.of()));
    }

    private static AssessmentRoleRequirement requirement(String role, String competency,
                                                         AssessmentRequirementImportance importance, Integer expected) {
        return new AssessmentRoleRequirement(new AssessmentRoleRequirementId(MATRIX, role, competency),
                importance, expected == null ? null : expected.shortValue());
    }

    private static MatchingEngineV2.RoleScore scoreFor(List<MatchingEngineV2.RoleScore> scores, String roleId) {
        return scores.stream().filter(score -> score.roleId().equals(roleId)).findFirst().orElseThrow();
    }

    private List<MatchingEngineV2.RoleScore> calculate(List<AssessmentRoleRequirement> requirements,
                                                       Map<String, Integer> answers) {
        List<AssessmentRoleRequirement> completeMatrix = new ArrayList<>(requirements);
        Map<String, Integer> completeAnswers = new HashMap<>(answers);
        for (int i = 0; i < ROLES.size(); i++) {
            String roleId = ROLES.get(i);
            if (completeMatrix.stream().noneMatch(requirement -> requirement.id.roleId.equals(roleId))) {
                String code = "filler-" + i;
                completeMatrix.add(requirement(roleId, code, AssessmentRequirementImportance.ESSENTIAL, 1));
                completeAnswers.put(code, 0);
            }
        }
        return engine.calculate(ROLES, completeMatrix, completeAnswers);
    }
}
