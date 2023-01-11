package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalEnvironment extends BaseEntity {
    private EnvironmentName environmentName;
    private UUID portalId;
    private UUID portalEnvironmentConfigId;
    private PortalEnvironmentConfig portalEnvironmentConfig;
    private UUID siteContentId;
    private SiteContent siteContent;
    private UUID preRegSurveyId;
    private Survey preRegSurvey;
}
