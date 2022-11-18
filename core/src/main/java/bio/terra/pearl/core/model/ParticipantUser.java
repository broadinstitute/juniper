package bio.terra.pearl.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter @Setter @SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantUser extends BaseEntity {
    private String username;
    @Builder.Default
    private boolean superuser = false;

    private String token;

    private Instant lastLogin;
}
