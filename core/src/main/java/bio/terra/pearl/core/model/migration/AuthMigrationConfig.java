package bio.terra.pearl.core.model.migration;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AuthMigrationConfig extends BaseEntity {

    private UUID studyEnvironmentConfigId;

    @Builder.Default
    private AuthProvider authProvider = AuthProvider.AUTH0;

    @Builder.Default
    private String domain = "";

    @Builder.Default
    private String clientId = "";

    @Builder.Default
    private String clientSecret = "";

    @Builder.Default
    private String jwtPublicKey = "";

}
