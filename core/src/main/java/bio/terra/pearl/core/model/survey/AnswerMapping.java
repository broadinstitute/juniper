package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Builder;
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
    private String formatString; // format of the source (e.g. mm/dd/yyyy for a mm/dd/yyyy field in a survey
    @Builder.Default
    private boolean errorOnFail = false; // whether to throw an error on parse failures, or just log it and continue
}
