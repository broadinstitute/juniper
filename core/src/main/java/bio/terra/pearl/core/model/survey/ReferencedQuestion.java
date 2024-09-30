package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
// Keeps track of referenced questions that exist outside a survey but are still needed for rendering
public class ReferencedQuestion extends BaseEntity {
    private UUID surveyId;
    private String referencedSurveyStableId;
    private String referencedQuestionStableId;
}
