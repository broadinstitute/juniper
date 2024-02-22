package bio.terra.pearl.populate.dto.i18n;

import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PortalEnvironmentLanguagePopDto extends PortalEnvironmentLanguage {
    private String languageTextsFileName;
}
