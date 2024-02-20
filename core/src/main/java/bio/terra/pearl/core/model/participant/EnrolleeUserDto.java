package bio.terra.pearl.core.model.participant;

import com.azure.core.annotation.Get;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** holder class for an enrollee and related user entities */
@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class EnrolleeUserDto {
    private Enrollee enrollee;
    private ParticipantUser participantUser;
    private PortalParticipantUser portalParticipantUser;
}
