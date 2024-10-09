package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalParticipantUser extends BaseEntity {
    private ParticipantUser participantUser;
    private UUID participantUserId;
    private PortalEnvironment portalEnvironment;
    private UUID portalEnvironmentId;
    private Profile profile;
    private UUID profileId;
    private Instant lastLogin;
}
