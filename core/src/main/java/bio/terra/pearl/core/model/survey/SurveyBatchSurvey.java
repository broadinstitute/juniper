package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@NoArgsConstructor @Setter @Getter @SuperBuilder
public class SurveyBatchSurvey extends BaseEntity {
    private UUID surveyBatchId;
    private UUID surveyId;
    private Survey survey;
    private int surveyOrder;
    private String rule;
    @Builder.Default
    private boolean allowAdminEdit = true;
    @Builder.Default
    private boolean allowParticipantStart = true;
    @Builder.Default
    private boolean allowParticipantReedit = true;
    @Builder.Default
    private boolean prepopulate = false;
}
