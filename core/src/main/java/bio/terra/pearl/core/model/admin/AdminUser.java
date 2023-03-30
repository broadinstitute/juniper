package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class AdminUser extends BaseEntity {
    // TODO: extract a User entity to share with ParticipantUser?
    private String username;

    private String token;

    private Instant lastLogin;
    @Builder.Default
    private boolean superuser = false;
}
