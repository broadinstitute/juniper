package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.model.site.NavbarItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.UUID;

@Configuration
public class ExtractionObjectMapperConfig {

    private final ObjectMapper objectMapper;
    public ExtractionObjectMapperConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.addMixIn(BaseEntity .class, BaseEntityMixin.class);
        this.objectMapper.addMixIn(NavbarItem .class, NavbarItemMixin.class);
        this.objectMapper.addMixIn(HtmlSection.class, HtmlSectionMixin.class);
    }

    @Bean(name = "extractionObjectMapper")
    public ObjectMapper getExtractionObjectMapper() {
        return objectMapper;
    }

    /** mixin to ignore the id and timestamps when serializing */
    protected static class BaseEntityMixin {
        @JsonIgnore
        public UUID getId() {return null;}
        @JsonIgnore
        public Instant getCreatedAt() {return null;}
        @JsonIgnore
        public Instant getLastUpdatedAt() {return null;}
    }

    /** don't serialize the item order -- it's captured in the order of the list */
    protected static class NavbarItemMixin {
        @JsonIgnore
        public UUID getItemOrder() {return null;}
    }

    /** don't serialize the section order -- it's captured in the order of the list */
    protected static class HtmlSectionMixin {
        @JsonIgnore
        public UUID getSectionOrder() {return null;}
    }

}
