package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** ObjectMapper with mixins to ignore the id and timestamps and other properties that should be
 * excluded when writing models to json files */
@Configuration
public class ExtractionObjectMapperConfig {
    private final ObjectMapper objectMapper;
    public ExtractionObjectMapperConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.addMixIn(BaseEntity.class, BaseEntityMixin.class);
    }

    @Bean(name = "extractionObjectMapper")
    protected ObjectMapper getExtractionObjectMapper() {
        return objectMapper;
    }


    protected static class BaseEntityMixin {
        @JsonIgnore
        public UUID getId() {return null;}
        @JsonIgnore
        public Instant getCreatedAt() {return null;}
        @JsonIgnore
        public Instant getLastUpdatedAt() {return null;}
    }
}
