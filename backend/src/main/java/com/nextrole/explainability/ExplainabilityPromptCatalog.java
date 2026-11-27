package com.nextrole.explainability;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ExplainabilityPromptCatalog {
    private static final Map<String, String> RESOURCES = Map.of(
            "explainability-v5", "prompts/explainability-v5.md");

    public PromptResource load(String version) {
        String resource = RESOURCES.get(version);
        if (resource == null) throw new IllegalArgumentException("Unknown Explainability prompt version");
        try (InputStream input = new ClassPathResource(resource).getInputStream()) {
            byte[] bytes = input.readAllBytes();
            return new PromptResource(version, new String(bytes, java.nio.charset.StandardCharsets.UTF_8), sha256(bytes));
        } catch (IOException e) {
            throw new IllegalStateException("Configured Explainability prompt resource is unavailable", e);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public record PromptResource(String version, String content, String sha256) {}
}
