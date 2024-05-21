package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EmailTemplate extends BaseEntity implements Versioned {
    private String stableId;
    private String name;
    private int version;
    private Integer publishedVersion;
    @Builder.Default
    private List<LocalizedEmailTemplate> localizedEmailTemplates = new ArrayList<>();
    private UUID portalId;

    public Optional<LocalizedEmailTemplate> getTemplateForLanguage(String language) {
        return this.getLocalizedEmailTemplates().stream().filter(template -> template.getLanguage().equals(language)).findFirst();
    }
}
