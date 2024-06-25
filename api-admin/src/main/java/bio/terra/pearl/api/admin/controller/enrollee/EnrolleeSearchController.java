package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeSearchApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeSearchExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrolleeSearchController implements EnrolleeSearchApi {
  private AuthUtilService authUtilService;
  private EnrolleeSearchExtService enrolleeSearchExtService;
  private HttpServletRequest request;

  public EnrolleeSearchController(
      AuthUtilService authUtilService,
      EnrolleeSearchExtService enrolleeSearchExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.enrolleeSearchExtService = enrolleeSearchExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> getExpressionSearchFacets(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.enrolleeSearchExtService.getExpressionSearchFacets(
            user, portalShortcode, studyShortcode, EnvironmentName.valueOf(envName)));
  }

  @Override
  public ResponseEntity<Object> executeSearchExpression(
      String portalShortcode, String studyShortcode, String envName, String expression) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.enrolleeSearchExtService.executeSearchExpression(
            user, portalShortcode, studyShortcode, EnvironmentName.valueOf(envName), expression));
  }
}
