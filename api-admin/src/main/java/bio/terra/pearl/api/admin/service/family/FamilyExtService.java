package bio.terra.pearl.api.admin.service.family;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.FamilyService;
import org.springframework.stereotype.Service;

@Service
public class FamilyExtService {
  private final FamilyService familyService;

  public FamilyExtService(FamilyService familyService) {
    this.familyService = familyService;
  }

  @EnforcePortalStudyEnvPermission(permission = "study_admin")
  public Family find(PortalStudyEnvAuthContext authContext, String familyShortcode) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();

    return familyService
        .findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvironment.getId())
        .map(familyService::loadForAdminView)
        .orElseThrow(() -> new NotFoundException("Family not found"));
  }
}
