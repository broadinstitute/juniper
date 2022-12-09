package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.SurveyBatch;
import bio.terra.pearl.core.model.survey.SurveyBatchSurvey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SurveyBatchPopDto extends SurveyBatch {
    private List<SurveyBatchSurveyPopDto> surveyBatchSurveyDtos;

    @Override
    public Set<SurveyBatchSurvey> getSurveyBatchSurveys() {
        return surveyBatchSurveyDtos.stream().collect(Collectors.toSet());
    }

}
