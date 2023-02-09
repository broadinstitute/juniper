package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeApi;
import bio.terra.pearl.api.admin.service.RequestUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrolleeController implements EnrolleeApi {
  private EnrolleeService enrolleeService;
  private RequestUtilService requestUtilService;
  private HttpServletRequest request;

  public EnrolleeController(
      EnrolleeService enrolleeService,
      RequestUtilService requestUtilService,
      HttpServletRequest request) {
    this.enrolleeService = enrolleeService;
    this.requestUtilService = requestUtilService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> search(
      String portalShortcode, String studyShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser adminUser = requestUtilService.getFromRequest(request);
    requestUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    List<EnrolleeSearchResult> enrolleeSearchResults =
        enrolleeService.search(studyShortcode, environmentName);
    return ResponseEntity.ok(enrolleeSearchResults);
  }

  @Override
  public ResponseEntity<Object> find(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser adminUser = requestUtilService.getFromRequest(request);
    requestUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    Optional<Enrollee> enrolleeOpt =
        enrolleeService.findByStudyEnvironmentAdminLoad(
            studyShortcode, environmentName, enrolleeShortcode);
    return ResponseEntity.of(enrolleeOpt.map(opt -> opt));
  }
}
