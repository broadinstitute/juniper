package bio.terra.pearl.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;


@Getter @Setter @SuperBuilder
public class Portal extends BaseEntity {
    private String name;

    private String shortcode;
    @Builder.Default
    private Set<PortalParticipantUser> portalParticipantUsers = new HashSet();
}
