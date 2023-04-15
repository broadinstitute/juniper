package bio.terra.pearl.core.model.survey;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AnswerChangeRecord {
    private UUID responsibleUserId; // id of the user making the change, if it was a participant
    private UUID responsibleAdminUserId; // id of the user making the change, if it was an admin
    private UUID enrolleeId; // id of impacted enrollee (may be null)
    private UUID portalParticipantUserId; // id of the impacted portal participant user
    private UUID surveyResponseId; // unique id to group operations
    private String questionStableId;
    private String surveyStableId;
    private int surveyVersion;
    private String oldValue;
    private String newValue;
}
