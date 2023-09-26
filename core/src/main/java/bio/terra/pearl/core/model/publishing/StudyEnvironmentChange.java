package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;

import java.util.List;

public record StudyEnvironmentChange(
        String studyShortcode,
        List<ConfigChange> configChanges,
        VersionedEntityChange<Survey> preEnrollSurveyChanges,
        ListChange<StudyEnvironmentConsent, VersionedConfigChange<ConsentForm>> consentChanges,
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges,
        ListChange<NotificationConfig, VersionedConfigChange<EmailTemplate>> notificationConfigChanges
){}
