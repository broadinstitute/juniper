package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class PortalRegistrationEvent implements BaseEvent {
    private ParticipantUser participantUser;
    private PortalParticipantUser newPortalUser;
    private PortalEnvironment portalEnvironment;
}
