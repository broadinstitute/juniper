package bio.terra.pearl.api.admin.models.dto;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalShallowDto {
  private UUID id;
  private String shortcode;
  private String name;
  private List<PortalStudy> portalStudies;
  private List<PortalEnvironment> portalEnvironments;
}
