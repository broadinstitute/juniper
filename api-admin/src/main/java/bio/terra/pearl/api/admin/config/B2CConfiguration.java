package bio.terra.pearl.api.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "b2c")
public record B2CConfiguration(String tenantName, String clientId) {}
