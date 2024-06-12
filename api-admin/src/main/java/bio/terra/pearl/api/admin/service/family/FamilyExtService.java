package bio.terra.pearl.api.admin.service.family;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.springframework.stereotype.Service;

@Service
public class FamilyExtService {
  private final FamilyService familyService;
  private final AuthUtilService authUtilService;
  private final StudyEnvironmentService studyEnvironmentService;

  public FamilyExtService(
      FamilyService familyService,
      AuthUtilService authUtilService,
      StudyEnvironmentService studyEnvironmentService) {
    this.familyService = familyService;
    this.authUtilService = authUtilService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public Family find(
      AdminUser operator,
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String familyShortcode) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);

    StudyEnvironment studyEnvironment =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(() -> new IllegalStateException("Study environment not found"));

    return familyService
        .findOneByShortcode(familyShortcode)
        .filter(f -> f.getStudyEnvironmentId().equals(studyEnvironment.getId()))
        .map(familyService::loadForAdminView)
        .orElseThrow(() -> new NotFoundException("Family not found"));
  }
}
