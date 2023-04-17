package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.SurveyResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class SurveyResponsePopDto extends SurveyResponse {
    private String surveyStableId;
    private int surveyVersion;
    private Set<AnswerPopDto> answerPopDtos;
    private Integer currentPageNo;
}
