package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;

import java.util.List;

public record StudyEnvironmentChange(
        String studyShortcode,
        List<ConfigChange> configChanges,
        VersionedEntityChange<Survey> preEnrollSurveyChanges,
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges,
        ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges
){}
