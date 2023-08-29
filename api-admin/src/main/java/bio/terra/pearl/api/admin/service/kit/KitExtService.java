package bio.terra.pearl.api.admin.service.kit;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KitExtService {
  private final AuthUtilService authUtilService;
  private final KitRequestService kitRequestService;
  private final StudyEnvironmentService studyEnvironmentService;

  public KitExtService(
      AuthUtilService authUtilService,
      KitRequestService kitRequestService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.kitRequestService = kitRequestService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public List<KitRequest> requestKits(
      AdminUser adminUser,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<String> enrolleeShortcodes,
      String kitType) {
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);

    return enrolleeShortcodes.stream()
        .map(enrolleeShortcode -> requestKit(adminUser, enrolleeShortcode, kitType))
        .toList();
  }

  public Collection<KitRequest> getKitRequestsByStudyEnvironment(
      AdminUser adminUser,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName) {
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();

    return kitRequestService.getSampleKitsByStudyEnvironment(studyEnvironment);
  }

  public KitRequest requestKit(AdminUser adminUser, String enrolleeShortcode, String kitTypeName) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(adminUser, enrolleeShortcode);
    return kitRequestService.requestKit(adminUser, enrollee, kitTypeName);
  }

  public Collection<KitRequest> getKitRequests(AdminUser adminUser, String enrolleeShortcode) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(adminUser, enrolleeShortcode);
    return kitRequestService.getKitRequests(enrollee);
  }
}
