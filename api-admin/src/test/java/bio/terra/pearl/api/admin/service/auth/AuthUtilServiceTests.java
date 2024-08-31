package bio.terra.pearl.api.admin.service.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AuthUtilServiceTests extends BaseSpringBootTest {
  @Autowired private AuthUtilService authUtilService;
  @Autowired private PortalService portalService;
  @Autowired private PortalAdminUserService portalAdminUserService;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalFactory portalFactory;
  @Autowired private RoleFactory roleFactory;
  @Autowired private PermissionFactory permissionFactory;
  @Autowired private PortalAdminUserRoleService portalAdminUserRoleService;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;

  @Test
  @Transactional
  public void authUserToPortalRejectsUsersNotInPortal(TestInfo info) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortal(user, portal.getShortcode());
        });

    // now add the user to a second portal
    Portal portal2 = portalFactory.buildPersisted(getTestName(info));
    portalAdminUserFactory.buildPersisted(getTestName(info), user.getId(), portal2.getId());
    // confirm user can access second portal
    Portal authedPortal = authUtilService.authUserToPortal(user, portal2.getShortcode());
    assertThat(authedPortal.getId(), equalTo(portal2.getId()));
    assertThat(portalService.checkAdminIsInPortal(user, portal.getId()), equalTo(false));
    assertThat(portalService.checkAdminIsInPortal(user, portal2.getId()), equalTo(true));

    // but still not the first
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortal(user, portal.getShortcode());
        });
  }

  @Test
  @Transactional
  public void authUserToPortalRejectsNotFoundPortal(TestInfo info) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortal(user, "DOES_NOT_EXIST");
        });
  }

  @Test
  @Transactional
  public void authUserToPortalAllowsSuperUser(TestInfo info) {
    AdminUser user =
        adminUserFactory.buildPersisted(
            adminUserFactory.builder(getTestName(info)).superuser(true));
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    assertThat(authUtilService.authUserToPortal(user, portal.getShortcode()), notNullValue());
  }

  @Test
  @Transactional
  public void authUserToPortalWithPermissionRejects(TestInfo info) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.authUserToPortalWithPermission(user, portal.getShortcode(), "tdr_export");
        });
  }

  @Test
  @Transactional
  public void authUserToPortalWithPermissionAllows(TestInfo info) {
    AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    permissionFactory.buildPersisted("delete_portal");
    Role portalDeleterRole =
        roleFactory.buildPersisted(getTestName(info), List.of("delete_portal"));
    DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(getTestName(info)).build();
    PortalAdminUser portalAdminUser =
        portalAdminUserService.create(
            PortalAdminUser.builder().adminUserId(user.getId()).portalId(portal.getId()).build(),
            auditInfo);

    portalAdminUserRoleService.setRoles(
        portalAdminUser.getId(), List.of(portalDeleterRole.getName()), auditInfo);

    assertThat(
        authUtilService
            .authUserToPortalWithPermission(user, portal.getShortcode(), "delete_portal")
            .getShortcode(),
        equalTo(portal.getShortcode()));
  }

  @Test
  public void testVerifyObjToPortal() {
    Portal portal = Portal.builder().id(UUID.randomUUID()).build();
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.verifyObjInPortal(portal, Optional.of(new Survey()));
        });
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          authUtilService.verifyObjInPortal(portal, Optional.empty());
        });

    Survey survey = Survey.builder().portalId(portal.getId()).build();
    authUtilService.verifyObjInPortal(portal, Optional.of(survey));
  }
}
