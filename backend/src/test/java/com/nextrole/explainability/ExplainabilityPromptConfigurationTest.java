package com.nextrole.explainability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

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
        assertEquals(1200, properties.getMaxOutputTokens());
        assertEquals(java.time.Duration.ofMinutes(2), properties.getLease());
    }
}
