package bio.terra.pearl.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class PortalParticipantUser extends BaseEntity {
    private UUID id;
    private ParticipantUser participantUser;
    private UUID userId;
    private Portal portal;
    private UUID portalId;
    private Instant createdAt;
}
