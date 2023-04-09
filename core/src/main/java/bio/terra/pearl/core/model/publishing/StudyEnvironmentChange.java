package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import java.util.List;

public record StudyEnvironmentChange(
        String studyShortcode,
        List<ConfigChange> configChanges,
        VersionedEntityChange preEnrollSurveyChanges,
        ListChange<StudyEnvironmentConsent, VersionedConfigChange> consentChanges,
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange> surveyChanges,
        ListChange<NotificationConfig, VersionedConfigChange> notificationConfigChanges
){}
