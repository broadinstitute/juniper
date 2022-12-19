package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyBatch;
import bio.terra.pearl.populate.dto.survey.SurveyBatchPopDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentPopDto extends StudyEnvironment {
    @Builder.Default
    private List<SurveyBatchPopDto> surveyBatchDtos = new ArrayList<>();
    @Override
    public Set<SurveyBatch> getSurveyBatches() {
        return surveyBatchDtos.stream().collect(Collectors.toSet());
    }
    @Builder.Default
    private List<String> enrolleeFiles = new ArrayList<>();
}
