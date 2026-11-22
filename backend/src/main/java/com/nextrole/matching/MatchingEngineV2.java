package com.nextrole.matching;

import com.nextrole.data.AssessmentRequirementImportance;
import com.nextrole.data.AssessmentRoleRequirement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Component;

/** Exact-rational scoring for versioned V2 assessment results. */
@Component
public final class MatchingEngineV2 {
    public static final String ALGORITHM_VERSION = "matching-v2.0";
    private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100);
    private final Random tieRandom;

    public MatchingEngineV2() {
        this(new Random());
    }

    MatchingEngineV2(Random tieRandom) {
        this.tieRandom = Objects.requireNonNull(tieRandom);
    }

    public record RoleScore(String roleId, BigInteger scoreNumerator, BigInteger scoreDenominator,
                            String displayPercentage, int rankPosition) {}

    public List<RoleScore> calculate(List<String> roleIds, Collection<AssessmentRoleRequirement> requirements,
                                     Map<String, Integer> answers) {
        Objects.requireNonNull(roleIds, "roleIds");
        Objects.requireNonNull(requirements, "requirements");
        Objects.requireNonNull(answers, "answers");
        if (roleIds.size() != 6 || new HashSet<>(roleIds).size() != 6) {
            throw new IllegalArgumentException("La matriz V2 debe contener exactamente seis roles distintos");
        }

        Map<String, List<AssessmentRoleRequirement>> byRole = new HashMap<>();
        for (String roleId : roleIds) byRole.put(roleId, new ArrayList<>());
        for (AssessmentRoleRequirement requirement : requirements) {
            String roleId = requirement.id.roleId;
            if (!byRole.containsKey(roleId)) {
                throw new IllegalArgumentException("La matriz contiene un rol que no pertenece al catálogo V2: " + roleId);
            }
            byRole.get(roleId).add(requirement);
        }

        List<UnrankedScore> scores = new ArrayList<>(6);
        for (String roleId : roleIds) {
            Rational weightedSum = Rational.ZERO;
            int weightTotal = 0;
            for (AssessmentRoleRequirement requirement : byRole.get(roleId)) {
                if (requirement.importance == AssessmentRequirementImportance.NOT_RELEVANT) continue;
                if (requirement.expectedLevel == null || requirement.expectedLevel < 1 || requirement.expectedLevel > 4) {
                    throw new IllegalArgumentException("Nivel esperado inválido para " + requirement.id.competencyCode);
                }
                Integer level = answers.get(requirement.id.competencyCode);
                if (level == null || level < 0 || level > 4) {
                    throw new IllegalArgumentException("Falta una respuesta válida para " + requirement.id.competencyCode);
                }
                int weight = requirement.importance == AssessmentRequirementImportance.ESSENTIAL ? 2 : 1;
                Rational contribution = level >= requirement.expectedLevel
                        ? Rational.ONE
                        : new Rational(BigInteger.valueOf(level), BigInteger.valueOf(requirement.expectedLevel));
                weightedSum = weightedSum.add(contribution.multiply(weight));
                weightTotal += weight;
            }
            if (weightTotal == 0) throw new IllegalArgumentException("El rol no tiene competencias relevantes: " + roleId);
            Rational percentage = weightedSum.multiply(ONE_HUNDRED)
                    .divide(BigInteger.valueOf(weightTotal));
            scores.add(new UnrankedScore(roleId, percentage));
        }

        scores.sort((left, right) -> right.percentage.compareTo(left.percentage));
        List<RoleScore> ranked = new ArrayList<>(6);
        int start = 0;
        while (start < scores.size()) {
            int end = start + 1;
            while (end < scores.size() && scores.get(start).percentage.compareTo(scores.get(end).percentage) == 0) end++;
            List<UnrankedScore> tieGroup = new ArrayList<>(scores.subList(start, end));
            if (tieGroup.size() > 1) Collections.shuffle(tieGroup, tieRandom);
            for (UnrankedScore score : tieGroup) {
                ranked.add(new RoleScore(score.roleId, score.percentage.numerator, score.percentage.denominator,
                        formatPercentage(score.percentage.numerator, score.percentage.denominator), ranked.size() + 1));
            }
            start = end;
        }
        return List.copyOf(ranked);
    }

    public static String formatPercentage(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() <= 0) throw new IllegalArgumentException("El denominador debe ser positivo");
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), 1, RoundingMode.HALF_UP).toPlainString();
    }

    private record UnrankedScore(String roleId, Rational percentage) {}

    private record Rational(BigInteger numerator, BigInteger denominator) implements Comparable<Rational> {
        private static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
        private static final Rational ONE = new Rational(BigInteger.ONE, BigInteger.ONE);

        private Rational {
            if (denominator.signum() == 0) throw new IllegalArgumentException("Denominador cero");
            if (denominator.signum() < 0) {
                numerator = numerator.negate();
                denominator = denominator.negate();
            }
            BigInteger gcd = numerator.gcd(denominator);
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }

        Rational add(Rational other) {
            return new Rational(numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
                    denominator.multiply(other.denominator));
        }

        Rational multiply(int multiplier) {
            return new Rational(numerator.multiply(BigInteger.valueOf(multiplier)), denominator);
        }

        Rational multiply(BigInteger multiplier) {
            return new Rational(numerator.multiply(multiplier), denominator);
        }

        Rational divide(BigInteger divisor) {
            return new Rational(numerator, denominator.multiply(divisor));
        }

        @Override
        public int compareTo(Rational other) {
            return numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator));
        }
    }
}
