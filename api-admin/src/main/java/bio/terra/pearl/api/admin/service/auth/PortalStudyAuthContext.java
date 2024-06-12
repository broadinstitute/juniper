package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.PortalStudy;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class PortalStudyAuthContext extends PortalAuthContext {
  private PortalStudy portalStudy;
  private String studyShortcode;

  public static PortalStudyAuthContext of(
      AdminUser operator, String portalShortcode, String studyShortcode) {
    return PortalStudyAuthContext.builder()
        .operator(operator)
        .portalShortcode(portalShortcode)
        .studyShortcode(studyShortcode)
        .build();
  }
}
