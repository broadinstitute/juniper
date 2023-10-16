package bio.terra.pearl.api.admin.controller.kit;

import bio.terra.pearl.api.admin.api.KitApi;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.kit.KitExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.service.kit.pepper.PepperApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class KitController implements KitApi {
  private final AuthUtilService authUtilService;
  private final KitExtService kitExtService;
  private final HttpServletRequest request;
  private final ObjectMapper objectMapper;

  public KitController(
      AuthUtilService authUtilService,
      KitExtService kitExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.kitExtService = kitExtService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> kitsByStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    var kits =
        kitExtService.getKitRequestsByStudyEnvironment(
            adminUser, portalShortcode, studyShortcode, environmentName);

    return ResponseEntity.ok(kits);
  }

  @ExceptionHandler(PepperApiException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorReport handlePepperApiException(PepperApiException e) {
    return new ErrorReport().statusCode(e.getHttpStatus().value()).message(e.getMessage());
  }

  @Override
  public ResponseEntity<Object> requestKit(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      String kitType) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    KitRequest sampleKit =
        kitExtService.requestKit(adminUser, studyShortcode, enrolleeShortcode, kitType);
    return ResponseEntity.ok(sampleKit);
  }

  @Override
  public ResponseEntity<Object> getKitRequests(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    var kitRequests = kitExtService.getKitRequests(adminUser, enrolleeShortcode);
    return ResponseEntity.ok(kitRequests);
  }

  @Override
  public ResponseEntity<Object> requestKits(
      String kitType, String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    var enrolleeShortcodes = Arrays.asList(objectMapper.convertValue(body, String[].class));
    var result =
        kitExtService.requestKits(
            adminUser,
            portalShortcode,
            studyShortcode,
            environmentName,
            enrolleeShortcodes,
            kitType);

    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Void> refreshKitStatuses(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    kitExtService.refreshKitStatuses(adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.noContent().build();
  }
}
