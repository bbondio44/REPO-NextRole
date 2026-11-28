package com.nextrole.explainability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

class ExplainabilityPromptConfigurationTest {
    @Test
    void exactV5ResourceMatchesApprovedHashAndContractReference() throws Exception {
        ExplainabilityPromptCatalog.PromptResource prompt = new ExplainabilityPromptCatalog().load("explainability-v5");
        Properties approval = new Properties();
        try (InputStream stream = new ClassPathResource("contracts/explainability-v5.properties").getInputStream()) {
            approval.load(stream);
        }
        assertEquals(approval.getProperty("prompt.sha256"), prompt.sha256());
        assertTrue(approval.getProperty("input.contract").endsWith("models.py#Context"));
        assertTrue(approval.getProperty("input.competency.contract").endsWith("models.py#CompetencyContext"));
        assertTrue(prompt.content().contains("NextRole"));
        assertThrows(IllegalArgumentException.class, () -> new ExplainabilityPromptCatalog().load("unknown-version"));
    }

    @Test
    void optionalProviderSecretDoesNotBlockConfigurationDefaults() {
        ExplainabilityProperties properties = new ExplainabilityProperties();
        assertEquals("openrouter", properties.getProvider());
        assertEquals("baidu/ernie-5.5-flash-preview:free", properties.getModel());
        assertEquals("explainability-v5", properties.getPromptVersion());
        assertEquals("", properties.getOpenrouterApiKey());
        assertEquals(java.time.Duration.ofSeconds(3), properties.getConnectTimeout());
        assertEquals(java.time.Duration.ofSeconds(45), properties.getResponseTimeout());
        assertEquals(5000, properties.getMaxOutputTokens());
        assertEquals(java.time.Duration.ofMinutes(2), properties.getLease());
    }

    @Test
    void restClientRequestFactoryAppliesConfiguredConnectionAndResponseTimeouts() {
        ExplainabilityProperties properties = new ExplainabilityProperties();
        var factory = new ExplainabilityConfiguration().requestFactory(properties);

        assertEquals(3000, ReflectionTestUtils.getField(factory, "connectTimeout"));
        assertEquals(45000, ReflectionTestUtils.getField(factory, "readTimeout"));
        properties.setConnectTimeout(java.time.Duration.ofMillis(800));
        properties.setResponseTimeout(java.time.Duration.ofSeconds(7));
        factory = new ExplainabilityConfiguration().requestFactory(properties);
        assertEquals(800, ReflectionTestUtils.getField(factory, "connectTimeout"));
        assertEquals(7000, ReflectionTestUtils.getField(factory, "readTimeout"));
    }
}
