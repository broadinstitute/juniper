package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PortalParticipantUserPopDto extends PortalParticipantUser {
    private ParticipantUserPopDto participantUser;
}
