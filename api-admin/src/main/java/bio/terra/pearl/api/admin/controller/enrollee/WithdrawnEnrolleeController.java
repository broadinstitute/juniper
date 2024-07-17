package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.WithdrawnEnrolleeApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.WithdrawnEnrolleeExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class WithdrawnEnrolleeController implements WithdrawnEnrolleeApi {

  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;
  private final WithdrawnEnrolleeExtService withdrawnEnrolleeExtService;

  public WithdrawnEnrolleeController(
      AuthUtilService authUtilService,
      WithdrawnEnrolleeExtService enrolleeExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.withdrawnEnrolleeExtService = enrolleeExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> getAll(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<WithdrawnEnrollee> enrollees =
        withdrawnEnrolleeExtService.getAll(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName));
    return ResponseEntity.ok(enrollees);
  }
}
