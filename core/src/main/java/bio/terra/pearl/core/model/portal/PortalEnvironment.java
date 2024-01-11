package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
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
public class PortalEnvironment extends BaseEntity {
    private EnvironmentName environmentName;
    private UUID portalId;
    private UUID portalEnvironmentConfigId;
    private PortalEnvironmentConfig portalEnvironmentConfig;
    private UUID siteContentId;
    private SiteContent siteContent;
    private UUID preRegSurveyId;
    private Survey preRegSurvey;
    @Builder.Default
    private List<Trigger> triggers = new ArrayList<>();
    @Builder.Default
    private List<ParticipantDashboardAlert> participantDashboardAlerts = new ArrayList<>();
}
