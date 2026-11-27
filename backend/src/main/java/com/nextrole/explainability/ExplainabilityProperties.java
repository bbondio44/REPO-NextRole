package com.nextrole.explainability;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nextrole.explainability")
public class ExplainabilityProperties {
    private String provider = "openrouter";
    private String model = "baidu/ernie-5.5-flash-preview:free";
    private String promptVersion = "explainability-v5";
    private String openrouterUrl = "https://openrouter.ai/api/v1/chat/completions";
    private String openrouterApiKey = "";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(45);
    private int maxOutputTokens = 1200;
    private Duration lease = Duration.ofMinutes(2);

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    public String getOpenrouterUrl() { return openrouterUrl; }
    public void setOpenrouterUrl(String openrouterUrl) { this.openrouterUrl = openrouterUrl; }
    public String getOpenrouterApiKey() { return openrouterApiKey; }
    public void setOpenrouterApiKey(String openrouterApiKey) { this.openrouterApiKey = openrouterApiKey; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(Duration responseTimeout) { this.responseTimeout = responseTimeout; }
    public int getMaxOutputTokens() { return maxOutputTokens; }
    public void setMaxOutputTokens(int maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
    public Duration getLease() { return lease; }
    public void setLease(Duration lease) { this.lease = lease; }
}
