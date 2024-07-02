package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * minimal set of data that we pre-load when processing enrollees for business logic and/or emails
 */
@Getter
@AllArgsConstructor
public class EnrolleeContext {
    private final Enrollee enrollee;
    private final Profile profile; // profile is assumed to have the mailing address attached
    private final ParticipantUser participantUser;
}
