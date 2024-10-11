package bio.terra.pearl.api.admin.service.auth;

import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.*;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnforcePortalEnrolleePermissionTest extends BaseSpringBootTest {
  @Autowired private EnforcePortalEnrolleePermissionTestBean testBean;
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;
  @Autowired private PermissionFactory permissionFactory;
  @Autowired private RoleFactory roleFactory;
  @Autowired private PortalAdminUserRoleService portalAdminUserRoleService;

  @Test
  @Transactional
  public void testEnforceEnrollee(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    // should work
    testBean.requiresParticipantDataView(
        PortalEnrolleeAuthContext.of(
            operator, portalShortcode, studyShortcode, environmentName, enrollee.getShortcode()));
  }

  @Test
  @Transactional
  public void testWrongEnvironmentThrows(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    // enrollee is in sandbox environment, but we are trying to access in irb
    assertThrows(
        NotFoundException.class,
        () ->
            testBean.requiresParticipantDataView(
                PortalEnrolleeAuthContext.of(
                    operator,
                    portalShortcode,
                    studyShortcode,
                    EnvironmentName.irb,
                    enrollee.getShortcode())));
  }

  @Test
  @Transactional
  public void testEnrolleeInDifferentPortalThrows(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee diffEnvEnrollee = enrolleeFactory.buildPersisted(getTestName(info));
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);

    // enrollee is in sandbox environment, but we are trying to access in irb
    assertThrows(
        NotFoundException.class,
        () ->
            testBean.requiresParticipantDataView(
                PortalEnrolleeAuthContext.of(
                    operator,
                    portalShortcode,
                    studyShortcode,
                    environmentName,
                    diffEnvEnrollee.getShortcode())));
  }

  @Test
  @Transactional
  public void testAdminNotInPortal(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info));

    assertThrows(
        NotFoundException.class,
        () ->
            testBean.requiresParticipantDataView(
                PortalEnrolleeAuthContext.of(
                    operator,
                    portalShortcode,
                    studyShortcode,
                    environmentName,
                    enrollee.getShortcode())));
  }

  @Test
  @Transactional
  public void testBasePermAdminInPortal(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    AdminUser operator =
        portalAdminUserFactory
            .buildPersistedWithPortals(getTestName(info), List.of(bundle.getPortal()))
            .user();

    testBean.baseMethod(
        PortalEnrolleeAuthContext.of(
            operator, portalShortcode, studyShortcode, environmentName, enrollee.getShortcode()));
  }

  @Test
  @Transactional
  public void testAdminDoesNotHavePermission(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    AdminUser operator =
        portalAdminUserFactory
            .buildPersistedWithPortals(getTestName(info), List.of(bundle.getPortal()))
            .user();

    assertThrows(
        PermissionDeniedException.class,
        () ->
            testBean.requiresParticipantDataView(
                PortalEnrolleeAuthContext.of(
                    operator,
                    portalShortcode,
                    studyShortcode,
                    environmentName,
                    enrollee.getShortcode())));
  }

  @Test
  @Transactional
  public void testUserInPortalWithPermission(TestInfo info) {
    StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    String portalShortcode = bundle.getPortal().getShortcode();
    String studyShortcode = bundle.getStudy().getShortcode();
    EnvironmentName environmentName = bundle.getStudyEnv().getEnvironmentName();

    Enrollee enrollee =
        enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv(), new Profile());

    AdminUserBundle operatorBundle =
        portalAdminUserFactory.buildPersistedWithPortals(
            getTestName(info), List.of(bundle.getPortal()));

    AdminUser operator = operatorBundle.user();
    UUID portalAdminUserId = operatorBundle.portalAdminUsers().get(0).getId();

    Role role =
        roleFactory.buildPersistedCreatePermissions(
            getTestName(info), List.of("participant_data_view"));
    DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(getTestName(info)).build();
    portalAdminUserRoleService.create(
        PortalAdminUserRole.builder()
            .portalAdminUserId(portalAdminUserId)
            .roleId(role.getId())
            .build(),
        auditInfo);

    testBean.requiresParticipantDataView(
        PortalEnrolleeAuthContext.of(
            operator, portalShortcode, studyShortcode, environmentName, enrollee.getShortcode()));
  }

  @Test
  public void testBaseMethodErrorsOnNull() {
    assertThrows(NotImplementedException.class, () -> testBean.baseMethod(null));
  }

  @Test
  public void testNoAuthContextErrors() {
    assertThrows(NotImplementedException.class, () -> testBean.noAuthContextMethod("someArg"));
  }
}
