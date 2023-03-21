package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
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
    private String questionType;
    private String choices;
    private boolean required;
}
