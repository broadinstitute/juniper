package bio.terra.pearl.api.admin.service.kit;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.pepper.PepperApiException;
import bio.terra.pearl.core.service.kit.pepper.PepperParseException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
public class KitExtService {
  private final KitRequestService kitRequestService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final StudyService studyService;
  private final EnrolleeService enrolleeService;

  public KitExtService(
      KitRequestService kitRequestService,
      StudyEnvironmentService studyEnvironmentService,
      StudyService studyService,
      EnrolleeService enrolleeService) {
    this.kitRequestService = kitRequestService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.studyService = studyService;
    this.enrolleeService = enrolleeService;
  }

  @Getter
  @SuperBuilder
  @NoArgsConstructor
  @Slf4j
  public static class KitRequestListResponse {
    private final List<KitRequestDto> kitRequests = new ArrayList<>();
    private final List<Exception> exceptions = new ArrayList<>();

    public void addKitRequest(KitRequestDto kitRequest) {
      kitRequests.add(kitRequest);
    }

    public void addException(Exception exception) {
      exceptions.add(exception);
    }
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public Collection<KitRequestDto> getKitRequestsByStudyEnvironment(
      PortalStudyEnvAuthContext authContext) {
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.verifyStudy(
            authContext.getStudyShortcode(), authContext.getEnvironmentName());
    return kitRequestService.getKitsByStudyEnvironment(studyEnvironment);
  }

  @EnforcePortalEnrolleePermission(permission = "BASE")
  public KitRequestDto requestKit(
      PortalEnrolleeAuthContext authContext,
      KitRequestService.KitRequestCreationDto kitRequestCreationDto) {
    return kitRequestService.requestKit(
        authContext.getOperator(),
        authContext.getStudyShortcode(),
        authContext.getEnrollee(),
        kitRequestCreationDto);
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public KitRequestListResponse requestKits(
      PortalStudyEnvAuthContext authContext,
      List<String> enrolleeShortcodes,
      KitRequestService.KitRequestCreationDto kitRequestCreationDto) {
    KitRequestListResponse response = new KitRequestListResponse();
    for (String enrolleeShortcode : enrolleeShortcodes) {
      try {
        Enrollee enrollee =
            enrolleeService
                .findByShortcodeAndStudyEnvId(
                    enrolleeShortcode, authContext.getStudyEnvironment().getId())
                .orElseThrow(
                    () ->
                        new NotFoundException(
                            "Enrollee not found for enrolleeShortcode: " + enrolleeShortcode));
        KitRequestDto createdKit =
            kitRequestService.requestKit(
                authContext.getOperator(),
                authContext.getStudyShortcode(),
                enrollee,
                kitRequestCreationDto);
        response.addKitRequest(createdKit);
      } catch (Exception e) {
        // add the enrollee shortcode to the message for disambiguation.  Once we refine the UX for
        // this, a structured response might be useful here
        response.addException(new Exception(enrolleeShortcode + ": " + e.getMessage(), e));
      }
    }
    return response;
  }

  @EnforcePortalEnrolleePermission(permission = "BASE")
  public KitRequest collectKit(
      PortalEnrolleeAuthContext authContext, KitRequestService.KitCollectionDto kitCollectionDto) {

    KitRequest kitRequest =
        kitRequestService.findByEnrolleeAndBarcode(
            authContext.getEnrollee(), kitCollectionDto.kitLabel());

    kitRequest.setReturnTrackingNumber(kitCollectionDto.returnTrackingNumber());

    return kitRequestService.collectKit(
        authContext.getOperator(), authContext.getStudyShortcode(), kitRequest);
  }

  @EnforcePortalEnrolleePermission(permission = "BASE")
  public Collection<KitRequestDto> getKitRequests(PortalEnrolleeAuthContext authContext) {
    return kitRequestService.findByEnrollee(authContext.getEnrollee());
  }

  @EnforcePortalStudyEnvPermission(permission = "BASE")
  public void refreshKitStatuses(PortalStudyEnvAuthContext authContext)
      throws PepperApiException, PepperParseException {
    Study study = studyService.find(authContext.getPortalStudy().getStudyId()).get();
    kitRequestService.syncKitStatusesForStudyEnv(study, authContext.getEnvironmentName());
  }
}
