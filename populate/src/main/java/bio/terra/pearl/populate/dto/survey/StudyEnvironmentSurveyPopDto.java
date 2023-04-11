package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dto so that we can specify the survey by stableId/Version or filename rather than id */
@Getter @Setter @NoArgsConstructor
public class StudyEnvironmentSurveyPopDto extends StudyEnvironmentSurvey implements FilePopulatable {
    private String surveyStableId;
    private int surveyVersion;
    private String populateFileName;
}
