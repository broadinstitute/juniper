package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


/** Includes a survey in an environment and configures scheduling and who can take it */
@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class StudyEnvironmentSurvey extends BaseEntity implements VersionedEntityConfig, StudyEnvAttached {
    private UUID studyEnvironmentId;
    private UUID surveyId;
    @Builder.Default
    private boolean active = true; // whether this represents a current configuration
    private Survey survey;
    private int surveyOrder; // what order the survey will be given in, compared to other surveys triggered at the same time
    @Override
    public Versioned versionedEntity() {
        return survey;
    }
    @Override
    public UUID versionedEntityId() { return surveyId; }
    @Override
    public void updateVersionedEntityId(UUID surveyId) {
        setSurveyId(surveyId);
    }
}
