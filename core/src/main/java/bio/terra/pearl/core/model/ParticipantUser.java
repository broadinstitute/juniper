package bio.terra.pearl.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class ParticipantUser extends BaseEntity {
    private String username;

    private String token;

    private Instant lastLogin;

    @Builder.Default
    private boolean withdrawn = false;

    private EnvironmentName environmentName;

    private Environment environment;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.environmentName = environment.getName();
    }

    @Builder.Default
    private Set<PortalParticipantUser> portalParticipantUsers = new HashSet<>();

    public static abstract class ParticipantUserBuilder<C extends ParticipantUser, B extends ParticipantUser.ParticipantUserBuilder<C, B>>
                extends BaseEntity.BaseEntityBuilder<C, B> {
        private Environment environment;
        private EnvironmentName environmentName;
        public ParticipantUserBuilder environment(Environment environment) {
            this.environment = environment;
            this.environmentName = environment.getName();
            return this;
        }
    }
}




