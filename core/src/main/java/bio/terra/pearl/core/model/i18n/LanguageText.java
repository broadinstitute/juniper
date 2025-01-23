package bio.terra.pearl.core.model.i18n;

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
public class LanguageText extends BaseEntity {
    private String keyName;
    private String text;
    private String language;


    // can be portal or site content attached. if
    // neither, then it's a global system text.
    // if site content attached, it will go through
    // standard publishing workflows.

    // nullable; if null, it's the default language text.
    //           if specified, then it's a portal-specific
    //           override for the default language text
    private UUID localizedSiteContentId;
    private UUID portalId;
}
