package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
        @Getter
        private Environment environment;
        @Getter
        private EnvironmentName environmentName;

        public ParticipantUserBuilder environment(Environment environment) {
            this.environment = environment;
            this.environmentName = environment.getName();
            return this;
        }
    }
}




