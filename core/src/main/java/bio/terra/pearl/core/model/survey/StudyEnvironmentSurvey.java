package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class StudyEnvironmentSurvey extends BaseEntity {
    private UUID studyEnvironmentId;
    private UUID surveyId;
    private Survey survey;
}
