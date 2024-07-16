package bio.terra.pearl.api.participant.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class PortalBrandingDto {
  private String dashboardBackgroundColor;
  private String navLogoCleanFileName;
  private int navLogoVersion;
  private String primaryBrandColor;
}
