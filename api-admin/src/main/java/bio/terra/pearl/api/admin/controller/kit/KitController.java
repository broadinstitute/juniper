package bio.terra.pearl.api.admin.controller.kit;

import bio.terra.pearl.api.admin.api.KitApi;
import bio.terra.pearl.api.admin.controller.GlobalExceptionHandler;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.kit.KitExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.pepper.PepperApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName);

    Collection<KitRequestDto> kits = kitExtService.getKitRequestsByStudyEnvironment(authContext);

    return ResponseEntity.ok(kits);
  }

  @ExceptionHandler(PepperApiException.class)
  public ResponseEntity<ErrorReport> handlePepperApiException(PepperApiException e) {
    return GlobalExceptionHandler.badRequestHandler(e, request);
  }

  @Override
  public ResponseEntity<Object> getKitRequests(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);

    PortalEnrolleeAuthContext authContext =
        PortalEnrolleeAuthContext.of(
            operator,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            enrolleeShortcode);

    Collection<KitRequestDto> kitRequests = kitExtService.getKitRequests(authContext);
    return ResponseEntity.ok(kitRequests);
  }

  @Override
  public ResponseEntity<Object> requestKit(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    KitRequestService.KitRequestCreationDto creationDto =
        objectMapper.convertValue(body, KitRequestService.KitRequestCreationDto.class);

    PortalEnrolleeAuthContext authContext =
        PortalEnrolleeAuthContext.of(
            operator,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            enrolleeShortcode);

    KitRequestDto sampleKit = kitExtService.requestKit(authContext, creationDto);
    return ResponseEntity.ok(sampleKit);
  }

  @Override
  public ResponseEntity<Object> collectKit(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcode,
      Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    KitRequestService.KitCollectionDto collectionDto =
        objectMapper.convertValue(body, KitRequestService.KitCollectionDto.class);

    PortalEnrolleeAuthContext authContext =
        PortalEnrolleeAuthContext.of(
            operator,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName),
            enrolleeShortcode);

    KitRequest sampleKit = kitExtService.collectKit(authContext, collectionDto);
    return ResponseEntity.ok(sampleKit);
  }

  @Override
  public ResponseEntity<Object> requestKits(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    KitRequestListCreationDto listCreationDto =
        objectMapper.convertValue(body, KitRequestListCreationDto.class);

    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(operator, portalShortcode, studyShortcode, environmentName);

    KitExtService.KitRequestListResponse result =
        kitExtService.requestKits(
            authContext, listCreationDto.enrolleeShortcodes, listCreationDto.creationDto);

    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<Void> refreshKitStatuses(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    PortalStudyEnvAuthContext authContext =
        PortalStudyEnvAuthContext.of(
            operator,
            portalShortcode,
            studyShortcode,
            EnvironmentName.valueOfCaseInsensitive(envName));
    kitExtService.refreshKitStatuses(authContext);
    return ResponseEntity.noContent().build();
  }

  public record KitRequestListCreationDto(
      KitRequestService.KitRequestCreationDto creationDto, List<String> enrolleeShortcodes) {}
}
