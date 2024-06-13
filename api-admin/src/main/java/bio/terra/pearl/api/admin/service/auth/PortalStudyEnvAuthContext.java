package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class PortalStudyEnvAuthContext extends PortalStudyAuthContext {
  EnvironmentName environmentName;
  StudyEnvironment studyEnvironment;

  public static PortalStudyEnvAuthContext of(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    return PortalStudyEnvAuthContext.builder()
        .operator(operator)
        .portalShortcode(portalShortcode)
        .studyShortcode(studyShortcode)
        .environmentName(environmentName)
        .build();
  }
}
