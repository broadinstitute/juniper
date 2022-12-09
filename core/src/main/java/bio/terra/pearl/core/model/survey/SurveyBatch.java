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

/**
 * Represents a batch of surveys that are all to be delivered at a specific time.
 * For example, there might be a batch of surveys to be delivered to participants immediately after consenting.
 */
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
