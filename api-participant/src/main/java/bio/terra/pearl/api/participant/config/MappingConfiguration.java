package bio.terra.pearl.api.participant.config;

import bio.terra.pearl.api.participant.models.mixins.DoNotSerializeResearchIdMixin;
import bio.terra.pearl.core.model.participant.Enrollee;
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
    return ObjectMapperUtils.buildObjectMapper()
        .addMixIn(Enrollee.class, DoNotSerializeResearchIdMixin.class);
  }
}
