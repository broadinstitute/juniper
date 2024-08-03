package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.populate.dto.TimeShiftable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class SurveyResponsePopDto extends SurveyResponse implements TimeShiftable {
    private String surveyStableId;
    private int surveyVersion;
    @Builder.Default
    private List<AnswerPopDto> answerPopDtos = new ArrayList<>();
    private Integer currentPageNo;
    private Integer submittedHoursAgo;
    private String justification;
    private String creatingAdminUsername;
}
