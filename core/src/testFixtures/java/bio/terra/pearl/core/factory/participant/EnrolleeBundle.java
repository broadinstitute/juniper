package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;

import java.util.UUID;

public record EnrolleeBundle(Enrollee enrollee, ParticipantUser participantUser,
                             PortalParticipantUser portalParticipantUser, UUID portalId) {
}
