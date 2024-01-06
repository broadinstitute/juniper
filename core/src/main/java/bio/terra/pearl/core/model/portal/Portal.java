package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.PortalStudy;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Portal extends BaseEntity {
    private String name;

    private String shortcode;
    @Builder.Default
    private List<PortalParticipantUser> portalParticipantUsers = new ArrayList();
    @Builder.Default
    private List<PortalStudy> portalStudies = new ArrayList<>();
    @Builder.Default
    private List<PortalEnvironment> portalEnvironments = new ArrayList<>();
}
