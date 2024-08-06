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

    // if this question & its subquestions are repeatable
    private Boolean repeatable;
    private Integer maxRepeats;

    // if this is a subquestion of a panel / json export, this is the parent question stable id
    private String parentQuestionStableId;
}
