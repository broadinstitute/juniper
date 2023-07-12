package bio.terra.pearl.api.admin.controller.kit;

import bio.terra.pearl.api.admin.api.KitApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeExtService;
import bio.terra.pearl.api.admin.service.kit.KitExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class KitController implements KitApi {
  private final AuthUtilService authUtilService;
  private final EnrolleeExtService enrolleeExtService;
  private final KitExtService kitExtService;
  private final HttpServletRequest request;

  public KitController(
      AuthUtilService authUtilService,
      EnrolleeExtService enrolleeExtService,
      KitExtService kitExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.enrolleeExtService = enrolleeExtService;
    this.kitExtService = kitExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> listForKitManagement(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    var enrollees =
        enrolleeExtService.findForKitManagement(
            adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(enrollees);
  }

  @Override
  public ResponseEntity<Object> listKitsForKitManagement(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    var kits =
        kitExtService.getKitRequestsForStudyEnvironment(
            adminUser, portalShortcode, studyShortcode, environmentName);

    return ResponseEntity.ok(kits);
  }
}
