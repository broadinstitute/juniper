package bio.terra.pearl.api.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSecurityConfig {
  private Environment env;

  public WebSecurityConfig(Environment env) {
    this.env = env;
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        String[] allowedCorsPatterns = env.getProperty("cors.enabled-path", String[].class);
        registry
            .addMapping("/**")
            .allowedOriginPatterns(allowedCorsPatterns)
            // update to support PATCH
            .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH");
      }
    };
  }
}
