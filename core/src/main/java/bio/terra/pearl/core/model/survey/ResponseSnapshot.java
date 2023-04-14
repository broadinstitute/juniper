package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * corresponds to a single update received from a user, either as an auto-save or a full submission
 */
@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class ResponseSnapshot extends BaseEntity {
    // either the adminUserId or participantUserId represents the user who submitted this data.  Only one should
    // exist for a given response
    private UUID creatingAdminUserId;
    private UUID creatingParticipantUserId;
    private UUID surveyResponseId;
    // a json representation of the diff, with __deleted_keys as an array of strings for deleted keys.
    // stored as a string for convenience and performance
    private String diff;
}
