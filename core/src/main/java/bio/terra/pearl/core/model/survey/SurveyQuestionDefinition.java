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
    @Builder.Default
    private boolean allowMultiple = false;
    @Builder.Default
    private boolean allowOtherDescription = false;
    private String choices;
    @Builder.Default
    private boolean required = false;
    private Integer exportOrder; // orders the questions on the export spreadsheet

    // if parentStableId is specified, then this question's value is derived from
    // the parent question's value by fetching parentValue[questionStableId], or,
    // if the parent is repeatable, parentValue[repeatIndex][questionStableId]
    private String parentStableId;
    private Boolean repeatable;
}
