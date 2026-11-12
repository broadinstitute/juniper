package bio.terra.pearl.api.admin.service.family;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ParticipantDataChange;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyExtService {
  private final FamilyService familyService;
  private final EnrolleeService enrolleeService;
  private final FamilyEnrolleeService familyEnrolleeService;
  private final EnrollmentService enrollmentService;

  public FamilyExtService(
      FamilyService familyService,
      EnrolleeService enrolleeService,
      FamilyEnrolleeService familyEnrolleeService,
      EnrollmentService enrollmentService) {
    this.familyService = familyService;
    this.enrolleeService = enrolleeService;
    this.familyEnrolleeService = familyEnrolleeService;
    this.enrollmentService = enrollmentService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public Family find(PortalStudyEnvAuthContext authContext, String familyShortcodeOrId) {
    StudyEnvironment studyEnvironment = authContext.getStudyEnvironment();

    if (familyShortcodeOrId.length() > 16) {
      return familyService
          .find(UUID.fromString(familyShortcodeOrId))
          .filter(family -> family.getStudyEnvironmentId().equals(studyEnvironment.getId()))
          .map(familyService::loadForAdminView)
          .orElseThrow(() -> new NotFoundException("Family not found"));
    }

    return familyService
        .findOneByShortcodeAndStudyEnvironmentId(familyShortcodeOrId, studyEnvironment.getId())
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

  /** request body for creating a new family member */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class NewFamilyMemberInfo {
    /** the existing family member the new member is related to */
    private String existingEnrolleeShortcode;

    /**
     * whether the new member is the target of the relation ("existing is the X of new"), or the
     * enrollee
     */
    private boolean newMemberIsTarget = true;

    private String familyRelationship;
    private Profile profile;
  }

  /**
   * Creates a new enrollee in the family, related to an existing member of the family. The new
   * enrollee cannot log in (see EnrollmentService#enrollFamilyMember).
   */
  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  @Transactional
  public Enrollee createMember(
      PortalStudyEnvAuthContext authContext,
      String familyShortcode,
      NewFamilyMemberInfo info,
      String justification) {
    if (info == null || StringUtils.isBlank(info.getExistingEnrolleeShortcode())) {
      throw new IllegalArgumentException("Existing enrollee is required");
    }
    if (StringUtils.isBlank(info.getFamilyRelationship())) {
      throw new IllegalArgumentException("Family relationship is required");
    }
    StudyEnvironment studyEnv = authContext.getStudyEnvironment();
    Family family =
        familyService
            .findOneByShortcodeAndStudyEnvironmentId(familyShortcode, studyEnv.getId())
            .orElseThrow(() -> new NotFoundException("Family not found"));
    Enrollee existing =
        enrolleeService
            .findByShortcodeAndStudyEnv(
                info.getExistingEnrolleeShortcode(),
                authContext.getStudyShortcode(),
                authContext.getEnvironmentName())
            .orElseThrow(() -> new NotFoundException("Enrollee not found"));
    if (familyEnrolleeService
        .findByFamilyIdAndEnrolleeId(family.getId(), existing.getId())
        .isEmpty()) {
      throw new IllegalArgumentException("Existing enrollee is not a member of the family");
    }
    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .familyId(family.getId())
            .justification(justification)
            .build();
    return enrollmentService.enrollFamilyMember(
        authContext.getEnvironmentName(),
        authContext.getStudyShortcode(),
        existing,
        info.isNewMemberIsTarget(),
        family.getId(),
        info.getFamilyRelationship(),
        info.getProfile(),
        auditInfo);
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
  public List<ParticipantDataChange> listChangeRecords(
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
    family.setStudyEnvironmentId(authContext.getStudyEnvironment().getId());

    Enrollee proband =
        enrolleeService
            .find(family.getProbandEnrolleeId())
            .orElseThrow(() -> new NotFoundException("Proband not found"));

    if (!proband.getStudyEnvironmentId().equals(family.getStudyEnvironmentId())) {
      throw new NotFoundException("Proband not found");
    }

    family.setShortcode(null);
    Family created =
        familyService.create(
            family,
            DataAuditInfo.builder()
                .responsibleAdminUserId(authContext.getOperator().getId())
                .justification(justification)
                .build());

    // if we don't also add the enrollee to the family, then
    // the family will be created without any enrollees
    this.addEnrollee(authContext, created.getShortcode(), proband.getShortcode(), justification);

    return find(authContext, created.getShortcode());
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  @Transactional
  public void delete(
      PortalStudyEnvAuthContext authContext, String familyShortcodeOrId, String justification) {
    AdminUser user = authContext.getOperator();

    Family family = find(authContext, familyShortcodeOrId);

    familyService.delete(
        family.getId(),
        DataAuditInfo.builder()
            .responsibleAdminUserId(user.getId())
            .justification(justification)
            .build());
  }
}
