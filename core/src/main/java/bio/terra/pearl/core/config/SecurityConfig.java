package bio.terra.pearl.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Configuration
public class SecurityConfig {
    @Bean
    public SecureRandom secureRandom() throws NoSuchAlgorithmException { return SecureRandom.getInstanceStrong(); }

}
