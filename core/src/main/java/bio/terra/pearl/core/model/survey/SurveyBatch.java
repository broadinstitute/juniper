package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class SurveyBatch extends BaseEntity {
    private UUID studyEnvironmentId;
    private UUID scheduleId;
    private Schedule schedule;
    @Builder.Default
    private Set<SurveyBatchSurvey> surveyBatchSurveys = new HashSet<>();
    private String rule;
    @Builder.Default
    private boolean strictOrder = false;
}
