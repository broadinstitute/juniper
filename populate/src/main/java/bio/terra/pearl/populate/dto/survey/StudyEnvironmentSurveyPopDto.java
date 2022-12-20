package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dto so that we can specify the survey by stableId/Version rather than id */
@Getter @Setter @NoArgsConstructor
public class StudyEnvironmentSurveyPopDto extends StudyEnvironmentSurvey {
    private String surveyStableId;
    private int surveyVersion;
}
