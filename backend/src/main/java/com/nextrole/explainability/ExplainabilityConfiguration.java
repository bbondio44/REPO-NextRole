package com.nextrole.explainability;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExplainabilityProperties.class)
public class ExplainabilityConfiguration {
    @Bean
    RestClient explainabilityRestClient(ExplainabilityProperties properties) {
        return RestClient.builder().requestFactory(requestFactory(properties)).build();
    }

    SimpleClientHttpRequestFactory requestFactory(ExplainabilityProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getResponseTimeout());
        return requestFactory;
    }
}
