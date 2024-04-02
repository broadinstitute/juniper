package bio.terra.pearl.api.admin.controller.search;

import bio.terra.pearl.api.admin.api.EnrolleeSearchExpressionApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrolleeSearchExpressionController implements EnrolleeSearchExpressionApi {
  private final EnrolleeSearchExpressionService enrolleeSearchExpressionService;
  private final AuthUtilService authUtilService;
  private final HttpServletRequest request;

  public EnrolleeSearchExpressionController(
      EnrolleeSearchExpressionService enrolleeSearchExpressionService,
      AuthUtilService authUtilService,
      HttpServletRequest request) {
    this.enrolleeSearchExpressionService = enrolleeSearchExpressionService;
    this.authUtilService = authUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> getSearchFacets(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);
    return ResponseEntity.ok(
            enrolleeSearchExpressionService.getSearchFacetsForPortal(
                    portalShortcode, EnvironmentName.valueOf(envName)));
  }
}
