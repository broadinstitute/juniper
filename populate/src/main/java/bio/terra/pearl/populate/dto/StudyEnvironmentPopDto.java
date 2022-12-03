package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyBatch;
import bio.terra.pearl.populate.dto.survey.SurveyBatchPopDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentPopDto extends StudyEnvironment {
    private List<SurveyBatchPopDto> surveyBatchDtos;

    @Override
    public Set<SurveyBatch> getSurveyBatches() {
        return surveyBatchDtos.stream().collect(Collectors.toSet());
    }
    private List<String> enrolleeFiles;
}
