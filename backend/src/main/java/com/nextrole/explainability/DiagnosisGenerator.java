package com.nextrole.explainability;

import java.math.BigDecimal;

/** Provider boundary for generating one raw structured diagnosis from the approved context and prompt. */
public interface DiagnosisGenerator {
    GenerationResult generate(ExplainabilityContext context, ExplainabilityPromptCatalog.PromptResource prompt);

    record GenerationResult(String rawResponse, String modelRequested, String modelServed,
                            String provider, Usage usage, Long durationMs) {
        public GenerationResult {
            if (rawResponse == null) throw new IllegalArgumentException("rawResponse is required");
            if (modelRequested == null || modelRequested.isBlank()) {
                throw new IllegalArgumentException("modelRequested is required");
            }
            if (provider == null || provider.isBlank()) throw new IllegalArgumentException("provider is required");
        }
    }

    /** Null fields mean that the provider did not report that metric. */
    record Usage(Long inputTokens, Long outputTokens, Long reasoningTokens, BigDecimal cost) {}

    enum FailureCode {
        PROVIDER_ERROR,
        PROVIDER_UNAVAILABLE,
        PROVIDER_TIMEOUT,
        INVALID_PROVIDER_RESPONSE
    }

    final class GenerationException extends RuntimeException {
        private final FailureCode failureCode;

        public GenerationException(FailureCode failureCode) {
            super(failureCode.name());
            this.failureCode = failureCode;
        }

        public FailureCode failureCode() { return failureCode; }
    }
}
