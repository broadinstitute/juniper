package bio.terra.pearl.api.admin.service.auth.context;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class PortalEnrolleeAuthContext extends PortalStudyEnvAuthContext
    implements EnvironmentAwareAuthContext {
  String enrolleeShortcodeOrId;
  Enrollee enrollee;

  public static PortalEnrolleeAuthContext of(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String enrolleeShortcodeOrId) {
    return PortalEnrolleeAuthContext.builder()
        .operator(operator)
        .portalShortcode(portalShortcode)
        .studyShortcode(studyShortcode)
        .environmentName(environmentName)
        .enrolleeShortcodeOrId(enrolleeShortcodeOrId)
        .build();
  }
}
