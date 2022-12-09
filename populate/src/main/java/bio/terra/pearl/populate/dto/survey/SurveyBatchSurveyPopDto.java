package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.SurveyBatchSurvey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SurveyBatchSurveyPopDto extends SurveyBatchSurvey {
    private String surveyStableId;
    private int surveyVersion;
}
