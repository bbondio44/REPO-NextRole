package com.nextrole.explainability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

@Component
public class OpenRouterDiagnosisGenerator implements DiagnosisGenerator {
    private final RestClient client;
    private final ExplainabilityProperties properties;
    private final ObjectMapper mapper;

    public OpenRouterDiagnosisGenerator(RestClient explainabilityRestClient, ExplainabilityProperties properties,
                                        ObjectMapper mapper) {
        this.client = explainabilityRestClient;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public GenerationResult generate(ExplainabilityContext context, ExplainabilityPromptCatalog.PromptResource prompt) {
        String apiKey = properties.getOpenrouterApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new GenerationException(FailureCode.PROVIDER_UNAVAILABLE);
        }
        if (prompt == null || prompt.content() == null || prompt.content().isBlank()) {
            throw new IllegalArgumentException("Configured Explainability prompt is empty");
        }

        long started = System.nanoTime();
        try {
            String body = client.post()
                    .uri(properties.getOpenrouterUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request(context, prompt))
                    .retrieve()
                    .body(String.class);
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            return parseResponse(body, durationMs);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 429 || e.getStatusCode().is5xxServerError()) {
                throw new GenerationException(FailureCode.PROVIDER_UNAVAILABLE);
            }
            throw new GenerationException(FailureCode.PROVIDER_ERROR);
        } catch (ResourceAccessException e) {
            if (hasTimeoutCause(e)) throw new GenerationException(FailureCode.PROVIDER_TIMEOUT);
            throw new GenerationException(FailureCode.PROVIDER_UNAVAILABLE);
        } catch (DiagnosisGenerator.GenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException(FailureCode.PROVIDER_ERROR);
        }
    }

    private ObjectNode request(ExplainabilityContext context, ExplainabilityPromptCatalog.PromptResource prompt) {
        ObjectNode request = mapper.createObjectNode();
        request.put("model", properties.getModel());
        request.put("stream", false);
        request.put("max_tokens", properties.getMaxOutputTokens());
        request.putObject("provider").put("allow_fallbacks", false);
        var messages = request.putArray("messages");
        messages.addObject().put("role", "system").put("content", prompt.content());
        messages.addObject().put("role", "user").put("content", context.canonicalJson(mapper));
        return request;
    }

    private GenerationResult parseResponse(String body, long durationMs) {
        try {
            JsonNode response = mapper.readTree(body);
            JsonNode choices = response == null ? null : response.path("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) throw invalidResponse();
            JsonNode content = choices.get(0).path("message").path("content");
            if (!content.isTextual()) throw invalidResponse();
            JsonNode usageNode = response.path("usage");
            Usage usage = usageNode.isObject() ? new Usage(
                    nullableLong(usageNode.get("prompt_tokens")),
                    nullableLong(usageNode.get("completion_tokens")),
                    nullableLong(usageNode.path("completion_tokens_details").get("reasoning_tokens")),
                    nullableDecimal(usageNode.get("cost"))) : null;
            String servedModel = response.path("model").isTextual() ? response.path("model").textValue() : null;
            return new GenerationResult(content.textValue(), properties.getModel(), servedModel,
                    "openrouter", usage, durationMs);
        } catch (GenerationException e) {
            throw e;
        } catch (Exception e) {
            throw invalidResponse();
        }
    }

    private static Long nullableLong(JsonNode value) {
        return value != null && value.canConvertToLong() ? value.longValue() : null;
    }

    private static BigDecimal nullableDecimal(JsonNode value) {
        return value != null && value.isNumber() ? value.decimalValue() : null;
    }

    private static boolean hasTimeoutCause(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException) return true;
        }
        return false;
    }

    private static GenerationException invalidResponse() {
        return new GenerationException(FailureCode.INVALID_PROVIDER_RESPONSE);
    }
}
