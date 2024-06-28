package bio.terra.pearl.api.admin.service.family;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import org.springframework.stereotype.Service;

@Service
public class FamilyExtService {
  private final FamilyService familyService;
  private final FamilyEnrolleeService familyEnrolleeService;

  public FamilyExtService(
      FamilyService familyService, FamilyEnrolleeService familyEnrolleeService) {
    this.familyService = familyService;
    this.familyEnrolleeService = familyEnrolleeService;
  }

  @EnforcePortalStudyEnvPermission(permission = "???")
  public Family find(PortalStudyEnvAuthContext authContext, String familyShortcode) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();

    return familyService
        .findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvironment.getId())
        .map(familyService::loadForAdminView)
        .orElseThrow(() -> new NotFoundException("Family not found"));
  }

  @EnforcePortalStudyEnvPermission(permission = "???")
  public void addEnrollee(
      PortalStudyEnvAuthContext authContext,
      String familyShortcode,
      String enrolleeShortcode,
      String justification) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();
    AdminUser user = authContext.getOperator();

    familyService.addEnrollee(
        familyShortcode,
        enrolleeShortcode,
        studyEnvironment.getId(),
        DataAuditInfo.builder()
            .responsibleAdminUserId(user.getId())
            .justification(justification)
            .build());
  }

  @EnforcePortalStudyEnvPermission(permission = "???")
  public void removeEnrollee(
      PortalStudyEnvAuthContext authContext,
      String familyShortcode,
      String enrolleeShortcode,
      String justification) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();
    AdminUser user = authContext.getOperator();

    familyService.removeEnrollee(
        familyShortcode,
        enrolleeShortcode,
        studyEnvironment.getId(),
        DataAuditInfo.builder()
            .responsibleAdminUserId(user.getId())
            .justification(justification)
            .build());
  }

  @EnforcePortalStudyEnvPermission(permission = "???")
  public Family updateProband(
      PortalStudyEnvAuthContext authContext,
      String familyShortcode,
      String enrolleeShortcode,
      String justification) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();
    AdminUser user = authContext.getOperator();

    return familyService.updateProband(
        familyShortcode,
        enrolleeShortcode,
        studyEnvironment.getId(),
        DataAuditInfo.builder()
            .responsibleAdminUserId(user.getId())
            .justification(justification)
            .build());
  }
}