package com.nextrole.explainability.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nextrole.explainability.DiagnosisGenerator;
import com.nextrole.explainability.ExplainabilityContext;
import com.nextrole.explainability.ExplainabilityPromptCatalog;

import java.util.concurrent.atomic.AtomicInteger;

/** Minimal deterministic fake for tests; it never performs I/O or reports provider usage. */
public class FakeDiagnosisGenerator implements DiagnosisGenerator {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger invocationCount = new AtomicInteger();
    private volatile ExplainabilityContext lastContext;

    @Override
    public GenerationResult generate(ExplainabilityContext context, ExplainabilityPromptCatalog.PromptResource prompt) {
        invocationCount.incrementAndGet();
        lastContext = context;
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

    public void reset() {
        invocationCount.set(0);
        lastContext = null;
    }
}
