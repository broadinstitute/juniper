package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
/**
 * Record of a change to a PortalEnvironment.
 * Currently, we only track changes to non-sandbox environments, which are performed via the diff/publishing workflow
 * */
public class PortalEnvironmentChange {
    VersionedEntityChange<SiteContent> siteContentChange;
    List<ConfigChange> configChanges;
    VersionedEntityChange<Survey> preRegSurveyChanges;
    ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges;
    List<ParticipantDashboardAlertChange> participantDashboardAlertChanges;
    List<StudyEnvironmentChange> studyEnvChanges;
    ListChange<PortalEnvironmentLanguage, Object> languageChanges
}
