package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.kit.StudyKitTypeService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudyExtService {
  private final AuthUtilService authUtilService;
  private final StudyKitTypeService studyKitTypeService;

  public StudyExtService(AuthUtilService authUtilService, StudyKitTypeService studyKitTypeService) {
    this.authUtilService = authUtilService;
    this.studyKitTypeService = studyKitTypeService;
  }

  public List<KitType> getKitTypes(
      AdminUser adminUser, String portalShortcode, String studyShortcode) {
    var portalStudy = authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    return studyKitTypeService.findKitTypesByStudyId(portalStudy.getStudyId());
  }
}
