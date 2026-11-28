package com.nextrole.explainability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenRouterDiagnosisGeneratorTest {
    private static final String ENDPOINT = "https://openrouter.test/api/v1/chat/completions";
    private static final String MODEL = "baidu/ernie-5.5-flash-preview:free";
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExplainabilityProperties properties = new ExplainabilityProperties();
    private final ExplainabilityPromptCatalog promptCatalog = new ExplainabilityPromptCatalog();
    private RestClient.Builder clientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        properties.setOpenrouterUrl(ENDPOINT);
        properties.setOpenrouterApiKey("test-only-secret");
        properties.setModel(MODEL);
        clientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(clientBuilder).build();
    }

    @Test
    void sendsConfiguredModelPromptJsonModeAndNoFallback() throws Exception {
        properties.setMaxOutputTokens(6000);
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-only-secret"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value(MODEL))
                .andExpect(jsonPath("$.stream").value(false))
                .andExpect(jsonPath("$.max_tokens").value(6000))
                .andExpect(jsonPath("$.provider.allow_fallbacks").value(false))
                .andExpect(jsonPath("$.models").doesNotExist())
                .andExpect(jsonPath("$.response_format.type").value("json_object"))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[0].content").value("approved prompt"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value(org.hamcrest.Matchers.containsString("\"roleId\":\"qa-jr\"")))
                .andRespond(withSuccess("""
                        {"id":"req-test","model":"served/model","choices":[{"message":{"content":"{\\"currentSituation\\":\\"Actual\\",\\"recommendedFocus\\":\\"Focus\\",\\"importanceExplanation\\":\\"Why\\",\\"professionalRelevance\\":\\"Work\\",\\"highlightedCompetencyIds\\":[\\"api-testing\\"]}"}}],"usage":{"prompt_tokens":100,"completion_tokens":40,"completion_tokens_details":{"reasoning_tokens":8},"cost":0.00001}}
                        """, MediaType.APPLICATION_JSON));

        var result = generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                "explainability-v5", "approved prompt", "a".repeat(64)));

        server.verify();
        assertEquals(MODEL, result.modelRequested());
        assertEquals("served/model", result.modelServed());
        assertEquals("openrouter", result.provider());
        assertEquals("{\"currentSituation\":\"Actual\",\"recommendedFocus\":\"Focus\",\"importanceExplanation\":\"Why\",\"professionalRelevance\":\"Work\",\"highlightedCompetencyIds\":[\"api-testing\"]}",
                result.rawResponse());
        assertEquals(100L, result.usage().inputTokens());
        assertEquals(40L, result.usage().outputTokens());
        assertEquals(8L, result.usage().reasoningTokens());
        assertEquals(0, new BigDecimal("0.00001").compareTo(result.usage().cost()));
        assertNotNull(result.durationMs());
    }

    @Test
    void classifiesTruncatedProviderOutputWithoutValidatingPartialJson() {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess("""
                {"choices":[{"finish_reason":"length","message":{"content":"{\\\"currentSituation\\\":"}}]}
                """, MediaType.APPLICATION_JSON));

        var error = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));

        server.verify();
        assertEquals("TRUNCATED_PROVIDER_RESPONSE", error.failureCode().name());
    }

    @Test
    void distinguishesEmptyContentFromMalformedProviderEnvelope() {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess(
                "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"  \"}}]}", MediaType.APPLICATION_JSON));
        var empty = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));
        server.verify();
        assertEquals("EMPTY_PROVIDER_CONTENT", empty.failureCode().name());

        server.reset();
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));
        var malformed = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));
        server.verify();
        assertEquals("MALFORMED_PROVIDER_RESPONSE", malformed.failureCode().name());
    }

    @Test
    void distinguishesProviderHttpRateLimitAndOtherHttpErrors() {
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                .body("sensitive provider body").contentType(MediaType.APPLICATION_JSON));
        var limited = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));
        server.verify();
        assertEquals("PROVIDER_RATE_LIMITED", limited.failureCode().name());
        assertFalse(limited.getMessage().contains("sensitive provider body"));

        server.reset();
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body("sensitive provider body").contentType(MediaType.APPLICATION_JSON));
        var clientError = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));
        server.verify();
        assertEquals("PROVIDER_HTTP_ERROR", clientError.failureCode().name());
    }

    @Test
    void mapsUpstreamRateLimitsToSanitizedFailureWithoutRetainingProviderBody() {
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                .body("provider-secret-detail").contentType(MediaType.APPLICATION_JSON));

        var error = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));

        server.verify();
        assertEquals("PROVIDER_RATE_LIMITED", error.failureCode().name());
        assertFalse(error.getMessage().contains("provider-secret-detail"));
    }

    @Test
    void rejectsMalformedProviderEnvelopeWithoutAssumingMissingUsageOrModel() {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess(
                "{\"choices\":[{\"message\":{\"content\":\"diagnosis\"}}]}", MediaType.APPLICATION_JSON));
        var result = generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                "explainability-v5", "approved prompt", "a".repeat(64)));
        server.verify();
        assertNull(result.modelServed());
        assertNull(result.usage());

        server.reset();
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        var invalid = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));
        server.verify();
        assertEquals("MALFORMED_PROVIDER_RESPONSE", invalid.failureCode().name());
    }

    @Test
    void rejectsMissingCredentialBeforeAnyHttpRequest() {
        properties.setOpenrouterApiKey("");

        var error = assertThrows(DiagnosisGenerator.GenerationException.class,
                () -> generator().generate(context(), new ExplainabilityPromptCatalog.PromptResource(
                        "explainability-v5", "approved prompt", "a".repeat(64))));

        assertEquals(DiagnosisGenerator.FailureCode.PROVIDER_UNAVAILABLE, error.failureCode());
        server.verify();
    }

    private OpenRouterDiagnosisGenerator generator() {
        return new OpenRouterDiagnosisGenerator(clientBuilder.build(), properties, mapper);
    }

    private static ExplainabilityContext context() {
        return new ExplainabilityContext("qa-jr", "QA", "QA role", BigDecimal.valueOf(70),
                List.of(new ExplainabilityCompetencyContext("api-testing", "Pruebas de APIs",
                        new ExplainabilityCompetencyContext.LevelContext(1, "Comprendo pruebas"),
                        new ExplainabilityCompetencyContext.LevelContext(2, "Puedo probar"),
                        ExplainabilityCompetencyContext.Classification.ESSENTIAL, 1, 2)));
    }
}
