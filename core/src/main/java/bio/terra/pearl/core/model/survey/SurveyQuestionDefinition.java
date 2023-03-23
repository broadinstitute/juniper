package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder @NoArgsConstructor
public class SurveyQuestionDefinition extends BaseEntity {
    private UUID surveyId;
    private String surveyStableId;
    private int surveyVersion;
    private String questionStableId;
    private String questionText;
    private String questionType;
    private String choices;
    @Builder.Default
    private boolean required = false;
}
