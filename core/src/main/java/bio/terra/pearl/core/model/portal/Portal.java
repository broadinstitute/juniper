package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.PortalStudy;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;


@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Portal extends BaseEntity {
    private String name;

    private String shortcode;
    @Builder.Default
    private Set<PortalParticipantUser> portalParticipantUsers = new HashSet();
    @Builder.Default
    private Set<PortalStudy> portalStudies = new HashSet<>();
    @Builder.Default
    private Set<PortalEnvironment> portalEnvironments = new HashSet<>();
}
