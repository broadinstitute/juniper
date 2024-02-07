package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalLanguage extends BaseEntity {
    private UUID portalId;
    private String languageCode;
    private String languageName;
}
