package bio.terra.pearl.core.model.notification;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;

import java.util.ArrayList;
import java.util.List;
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
    private int version;
    private Integer publishedVersion;
    @Builder.Default
    private List<LocalizedEmailTemplate> localizedEmailTemplates = new ArrayList<>();
    @Builder.Default
    private String defaultLanguage = "en";
    private UUID portalId;
}
