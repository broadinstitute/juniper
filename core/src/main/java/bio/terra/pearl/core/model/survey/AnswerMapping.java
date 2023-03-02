package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Information on where and how to map values to other places in the data model.  For example, a survey might want
 * to auto-map some responses to participant profile fields.
 * This will likely eventually need to support specifying all sorts of custom transformers for complex answers/objects
 * but for now simpleValue -> simpleValue is enough
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class AnswerMapping extends BaseEntity {
    private UUID surveyId;
    private String questionStableId;
    private AnswerMappingTargetType targetType;
    private String targetField;
    private AnswerMappingMapType mapType;
}
