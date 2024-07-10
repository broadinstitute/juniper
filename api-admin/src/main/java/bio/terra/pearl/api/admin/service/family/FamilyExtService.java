package bio.terra.pearl.api.admin.service.family;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.DataChangeRecord;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class FamilyExtService {
  private final FamilyService familyService;
  private final EnrolleeService enrolleeService;

  public FamilyExtService(FamilyService familyService, EnrolleeService enrolleeService) {
    this.familyService = familyService;
    this.enrolleeService = enrolleeService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public Family find(PortalStudyEnvAuthContext authContext, String familyShortcode) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();

    return familyService
        .findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvironment.getId())
        .map(familyService::loadForAdminView)
        .orElseThrow(() -> new NotFoundException("Family not found"));
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
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

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
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

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
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

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<Family> findAll(PortalStudyEnvAuthContext authContext) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();

    return familyService.findByStudyEnvironmentId(studyEnvironment.getId()).stream()
        .map(familyService::loadForAdminView)
        .toList();
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<DataChangeRecord> listChangeRecords(
      PortalStudyEnvAuthContext authContext, String familyShortcode, String modelName) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();

    Family family =
        familyService
            .findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnvironment.getId())
            .orElseThrow(() -> new NotFoundException("Family not found"));

    if (Objects.nonNull(modelName)) {
      return familyService.findDataChangeRecordsByFamilyIdAndModelName(family.getId(), modelName);
    }

    return familyService.findDataChangeRecordsByFamilyId(family.getId());
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public Family create(PortalStudyEnvAuthContext authContext, Family family, String justification) {
    if (!family.getStudyEnvironmentId().equals(authContext.getStudyEnvironment().getId())) {
      throw new NotFoundException("Study environment not found");
    }

    Enrollee proband =
        enrolleeService
            .find(family.getProbandEnrolleeId())
            .orElseThrow(() -> new NotFoundException("Proband not found"));

    if (!proband.getStudyEnvironmentId().equals(family.getStudyEnvironmentId())) {
      throw new NotFoundException("Proband not found");
    }

    AdminUser user = authContext.getOperator();

    family.setShortcode(null);
    Family created =
        familyService.create(
            family,
            DataAuditInfo.builder()
                .responsibleAdminUserId(user.getId())
                .justification(justification)
                .build());

    // if we don't also add the enrollee to the family, then
    // the family will be created without any enrollees
    this.addEnrollee(authContext, created.getShortcode(), proband.getShortcode(), justification);

    return find(authContext, created.getShortcode());
  }
}
