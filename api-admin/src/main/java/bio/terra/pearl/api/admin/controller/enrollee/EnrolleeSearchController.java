package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeSearchApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeSearchExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.search.EnrolleeSearchOptions;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
    AdminUser operator = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.enrolleeSearchExtService.getExpressionSearchFacets(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, EnvironmentName.valueOf(envName))));
  }

  @Override
  public ResponseEntity<Object> executeSearchExpression(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String expression,
      Integer limit,
      List<String> includes) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<EnrolleeSearchOptions.Include> includeList =
        includes == null
            ? List.of()
            : includes.stream().map(EnrolleeSearchOptions.Include::valueOf).toList();
    return ResponseEntity.ok(
        this.enrolleeSearchExtService.executeSearchExpression(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, EnvironmentName.valueOf(envName)),
            expression,
            limit,
            includeList));
  }
}
