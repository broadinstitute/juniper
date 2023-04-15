package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * an answer to a survey question
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Answer extends BaseEntity {
    // either the adminUserId or participantUserId represents the user who submitted this data.  Only one should
    // exist for a given answer
    private UUID creatingAdminUserId;
    private UUID creatingParticipantUserId;
    private UUID surveyResponseId;
    private UUID enrolleeId;
    private String questionStableId;
    private String surveyStableId;
    private int surveyVersion;
    // the value as json, stored as a string for convenience and performance
    private String value;
    // the value represented as a string.  so, e.g. "1" for 1.
    private String simpleValue;
}
