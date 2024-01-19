package bio.terra.pearl.api.admin.service.kit;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.kit.pepper.PepperApiException;
import bio.terra.pearl.core.service.kit.pepper.PepperParseException;
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
  private final AuthUtilService authUtilService;
  private final KitRequestService kitRequestService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final StudyService studyService;

  public KitExtService(
      AuthUtilService authUtilService,
      KitRequestService kitRequestService,
      StudyEnvironmentService studyEnvironmentService,
      StudyService studyService) {
    this.authUtilService = authUtilService;
    this.kitRequestService = kitRequestService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.studyService = studyService;
  }

  public KitRequestListResponse requestKits(
      AdminUser adminUser,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<String> enrolleeShortcodes,
      String kitType) {
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    KitRequestListResponse response = new KitRequestListResponse();
    for (String enrolleeShortcode : enrolleeShortcodes) {
      try {
        response.addKitRequest(requestKit(adminUser, studyShortcode, enrolleeShortcode, kitType));
      } catch (Exception e) {
        // add the enrollee shortcode to the message for disambiguation.  Once we refine the UX for
        // this,
        // a structured response might be useful here
        response.addException(new Exception(enrolleeShortcode + ": " + e.getMessage(), e));
      }
    }
    return response;
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

  public Collection<KitRequestDto> getKitRequestsByStudyEnvironment(
      AdminUser adminUser,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService.verifyStudy(studyShortcode, environmentName);
    return kitRequestService.getKitsByStudyEnvironment(studyEnvironment);
  }

  public KitRequestDto requestKit(
      AdminUser adminUser, String studyShortcode, String enrolleeShortcode, String kitTypeName) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(adminUser, enrolleeShortcode);
    return kitRequestService.requestKit(adminUser, studyShortcode, enrollee, kitTypeName);
  }

  public Collection<KitRequestDto> getKitRequests(AdminUser adminUser, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(adminUser, enrolleeShortcode);
    return kitRequestService.findByEnrollee(enrollee);
  }

  public void refreshKitStatuses(
      AdminUser adminUser,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName)
      throws PepperApiException, PepperParseException {
    var portalStudy = authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    var study = studyService.find(portalStudy.getStudyId()).get();
    kitRequestService.syncKitStatusesForStudyEnv(study, environmentName);
  }
}
