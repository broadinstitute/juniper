package bio.terra.pearl.populate.config;

import bio.terra.pearl.core.shared.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MappingConfiguration {
  @Bean("objectMapper")
  @Primary
  public ObjectMapper populateObjectMapper() {
    return ObjectMapperUtils.buildObjectMapper();
  }
}
