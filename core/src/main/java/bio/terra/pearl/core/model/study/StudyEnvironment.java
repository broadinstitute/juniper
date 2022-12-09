package bio.terra.pearl.core.model.study;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.SurveyBatch;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter @SuperBuilder
@NoArgsConstructor
public class StudyEnvironment extends BaseEntity {
    private EnvironmentName environmentName;
    private UUID studyId;

    private UUID studyEnvironmentConfigId;
    private StudyEnvironmentConfig studyEnvironmentConfig;
    @Builder.Default
    private Set<SurveyBatch> surveyBatches = new HashSet<>();
    @Builder.Default
    private Set<StudyEnvironmentSurvey> studyEnvironmentSurveys = new HashSet<>();
}
