package com.nextrole.explainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextrole.explainability.support.FakeDiagnosisGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisGeneratorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void fakeProducesAValidStructuredResponseWithoutExternalUsage() throws Exception {
        ExplainabilityContext context = new ExplainabilityContext("qa-jr", "QA", "QA role", BigDecimal.valueOf(70),
                List.of(new ExplainabilityCompetencyContext("api-testing", "Pruebas de APIs",
                        new ExplainabilityCompetencyContext.LevelContext(1, "Comprendo pruebas"),
                        new ExplainabilityCompetencyContext.LevelContext(2, "Puedo probar"),
                        ExplainabilityCompetencyContext.Classification.ESSENTIAL, 1, 2)));
        var prompt = new ExplainabilityPromptCatalog.PromptResource("explainability-v5", "prompt", "a".repeat(64));
        var result = new FakeDiagnosisGenerator().generate(context, prompt);
        var json = mapper.readTree(result.rawResponse());

        assertEquals(java.util.Set.of("currentSituation", "recommendedFocus", "importanceExplanation",
                        "professionalRelevance", "highlightedCompetencyIds"), new java.util.HashSet<>(fieldNames(json)));
        assertEquals("api-testing", json.get("highlightedCompetencyIds").get(0).asText());
        assertEquals("fake", result.modelRequested());
        assertEquals("fake", result.modelServed());
        assertNull(result.usage());
        assertEquals(0L, result.durationMs());
    }

    @Test
    void generationResultAllowsMissingProviderUsageWithoutInventingZeroCost() {
        var result = new DiagnosisGenerator.GenerationResult("{}", "model-x", null,
                "openrouter", null, 12L);

        assertNull(result.modelServed());
        assertNull(result.usage());
        assertEquals(12L, result.durationMs());
    }

    @Test
    void fakeCanProduceInvalidTimeoutUnavailableAndRateLimitedScenarios() {
        var fake = new FakeDiagnosisGenerator();
        var context = sampleContext();
        var prompt = new ExplainabilityPromptCatalog.PromptResource("explainability-v5", "prompt", "a".repeat(64));

        fake.setScenario(FakeDiagnosisGenerator.Scenario.INVALID_RESPONSE);
        assertThrows(IllegalArgumentException.class, () -> new DiagnosisValidator(mapper).validate(
                fake.generate(context, prompt).rawResponse(), Set.of("api-testing")));

        fake.setScenario(FakeDiagnosisGenerator.Scenario.TIMEOUT);
        var timeout = assertThrows(DiagnosisGenerator.GenerationException.class, () -> fake.generate(context, prompt));
        assertEquals(DiagnosisGenerator.FailureCode.PROVIDER_TIMEOUT, timeout.failureCode());

        fake.setScenario(FakeDiagnosisGenerator.Scenario.UNAVAILABLE);
        var unavailable = assertThrows(DiagnosisGenerator.GenerationException.class, () -> fake.generate(context, prompt));
        assertEquals(DiagnosisGenerator.FailureCode.PROVIDER_UNAVAILABLE, unavailable.failureCode());

        fake.setScenario(FakeDiagnosisGenerator.Scenario.RATE_LIMITED);
        var rateLimited = assertThrows(DiagnosisGenerator.GenerationException.class, () -> fake.generate(context, prompt));
        assertEquals(DiagnosisGenerator.FailureCode.PROVIDER_UNAVAILABLE, rateLimited.failureCode());
    }

    @Test
    void fakeDelayIsControlledByLatchesAndReleasesDeterministically() throws Exception {
        var fake = new FakeDiagnosisGenerator();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        fake.delayNextInvocation(entered, release);
        var prompt = new ExplainabilityPromptCatalog.PromptResource("explainability-v5", "prompt", "a".repeat(64));
        var executor = Executors.newSingleThreadExecutor();
        try {
            var future = executor.submit(() -> fake.generate(sampleContext(), prompt));
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            assertEquals(1, fake.invocationCount());
            release.countDown();
            assertNotNull(future.get(2, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    private static ExplainabilityContext sampleContext() {
        return new ExplainabilityContext("qa-jr", "QA", "QA role", BigDecimal.valueOf(70),
                List.of(new ExplainabilityCompetencyContext("api-testing", "Pruebas de APIs",
                        new ExplainabilityCompetencyContext.LevelContext(1, "Comprendo pruebas"),
                        new ExplainabilityCompetencyContext.LevelContext(2, "Puedo probar"),
                        ExplainabilityCompetencyContext.Classification.ESSENTIAL, 1, 2)));
    }

    private static List<String> fieldNames(com.fasterxml.jackson.databind.JsonNode node) {
        var names = new java.util.ArrayList<String>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
