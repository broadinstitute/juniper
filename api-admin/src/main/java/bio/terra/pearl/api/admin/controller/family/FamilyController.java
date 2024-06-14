package bio.terra.pearl.api.admin.controller.family;

import bio.terra.pearl.api.admin.api.FamilyApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.family.FamilyExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Family;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class FamilyController implements FamilyApi {

  private final AuthUtilService authUtilService;
  private final FamilyExtService familyExtService;
  private final HttpServletRequest request;

  public FamilyController(
      AuthUtilService authUtilService,
      FamilyExtService familyExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.familyExtService = familyExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> find(
      String portalShortcode,
      String studyShortcode,
      String environmentName,
      String familyShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);

    Family family =
        familyExtService.find(
            PortalStudyEnvAuthContext.of(
                operator,
                portalShortcode,
                studyShortcode,
                EnvironmentName.valueOfCaseInsensitive(environmentName)),
            familyShortcode);

    return ResponseEntity.ok(family);
  }
}
