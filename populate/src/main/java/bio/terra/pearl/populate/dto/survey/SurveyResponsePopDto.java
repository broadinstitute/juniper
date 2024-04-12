package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.populate.dto.TimeShiftable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class SurveyResponsePopDto extends SurveyResponse implements TimeShiftable {
    private String surveyStableId;
    private int surveyVersion;
    private List<AnswerPopDto> answerPopDtos;
    private Integer currentPageNo;
    private Integer submittedHoursAgo;
}
