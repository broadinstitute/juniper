package bio.terra.pearl.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class AdminUser extends BaseEntity {
    // TODO: extract a User entity to share with ParticipantUser?
    private String username;

    private String token;

    private Instant lastLogin;

    private Boolean superuser;
//    private Portal portal;
}
