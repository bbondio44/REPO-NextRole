package com.nextrole.explainability.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nextrole.explainability.DiagnosisGenerator;
import com.nextrole.explainability.ExplainabilityContext;
import com.nextrole.explainability.ExplainabilityPromptCatalog;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Minimal deterministic fake for tests; it never performs I/O or reports provider usage. */
public class FakeDiagnosisGenerator implements DiagnosisGenerator {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger invocationCount = new AtomicInteger();
    private final AtomicReference<DelayGate> nextDelay = new AtomicReference<>();
    private volatile ExplainabilityContext lastContext;
    private volatile Scenario scenario = Scenario.VALID;

    @Override
    public GenerationResult generate(ExplainabilityContext context, ExplainabilityPromptCatalog.PromptResource prompt) {
        invocationCount.incrementAndGet();
        lastContext = context;
        waitIfDelayed();
        switch (scenario) {
            case TIMEOUT -> throw new GenerationException(FailureCode.PROVIDER_TIMEOUT);
            case UNAVAILABLE, RATE_LIMITED -> throw new GenerationException(FailureCode.PROVIDER_UNAVAILABLE);
            case INVALID_RESPONSE -> { return result("not valid JSON"); }
            case VALID -> { }
        }
        String highlightedId = context.competencies().getFirst().id();
        try {
            ObjectNode responseNode = mapper.createObjectNode();
            responseNode.put("currentSituation", "Fortalezas técnicas vinculadas con " + context.roleName() + ".");
            responseNode.put("recommendedFocus", "Sería valioso profundizar en una capacidad relevante para el rol.");
            responseNode.put("importanceExplanation", "Esta capacidad ayuda a resolver tareas habituales del rol.");
            responseNode.put("professionalRelevance", "Su aplicación puede mostrarse al resolver un caso práctico.");
            responseNode.putArray("highlightedCompetencyIds").add(highlightedId);
            String response = mapper.writeValueAsString(responseNode);
            return new GenerationResult(response, "fake", "fake", "fake", null, 0L);
        } catch (Exception e) {
            throw new IllegalStateException("Fake diagnosis response could not be serialized", e);
        }
    }

    public int invocationCount() { return invocationCount.get(); }

    public ExplainabilityContext lastContext() { return lastContext; }

    public void setScenario(Scenario scenario) { this.scenario = scenario; }

    public void delayNextInvocation(CountDownLatch entered, CountDownLatch release) {
        if (!nextDelay.compareAndSet(null, new DelayGate(entered, release))) {
            throw new IllegalStateException("A delayed invocation is already configured");
        }
    }

    public void reset() {
        invocationCount.set(0);
        lastContext = null;
        scenario = Scenario.VALID;
        nextDelay.set(null);
    }

    private void waitIfDelayed() {
        DelayGate delay = nextDelay.getAndSet(null);
        if (delay == null) return;
        delay.entered().countDown();
        try {
            if (!delay.release().await(30, TimeUnit.SECONDS)) {
                throw new GenerationException(FailureCode.PROVIDER_TIMEOUT);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenerationException(FailureCode.PROVIDER_TIMEOUT);
        }
    }

    private static GenerationResult result(String response) {
        return new GenerationResult(response, "fake", "fake", "fake", null, 0L);
    }

    public enum Scenario { VALID, INVALID_RESPONSE, TIMEOUT, UNAVAILABLE, RATE_LIMITED }

    private record DelayGate(CountDownLatch entered, CountDownLatch release) {}
}
