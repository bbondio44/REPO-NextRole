package com.nextrole.explainability;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExplainabilityProperties.class)
public class ExplainabilityConfiguration {}
