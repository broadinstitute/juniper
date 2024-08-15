package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class StudyEnvironmentChange {
    String studyShortcode;
    List<ConfigChange> configChanges;
    VersionedEntityChange<Survey> preEnrollSurveyChanges;
    ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges;
    ListChange<Trigger, VersionedConfigChange<EmailTemplate>> triggerChanges;
    ListChange<KitType, KitType> kitTypeChanges;
}
