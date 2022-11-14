package bio.terra.javatemplate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "javatemplate.sam")
public record SamConfiguration(String basePath) {}
