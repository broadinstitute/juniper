package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class ParticipantUser extends BaseEntity {
    private String username;

    private String shortcode;

    private String token;

    private Instant lastLogin;
    @Builder.Default
    private boolean loginAllowed = true;

    @Builder.Default
    private boolean withdrawn = false;

    private EnvironmentName environmentName;

    @Builder.Default
    private List<PortalParticipantUser> portalParticipantUsers = new ArrayList<>();
}




