package bio.terra.pearl.api.admin.service.kit;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.Collection;
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
}
