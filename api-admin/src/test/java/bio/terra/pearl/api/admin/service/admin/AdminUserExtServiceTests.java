package bio.terra.pearl.api.admin.service.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserBundle;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AdminUserExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserExtService adminUserExtService;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;
  @Autowired private PortalFactory portalFactory;
  @Autowired private PortalAdminUserRoleService portalAdminUserRoleService;

  @Test
  @Transactional
  public void testGetAllRequiresSuperuser() {
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () -> adminUserExtService.getAll(AdminUser.builder().superuser(false).build()));
  }

  @Test
  @Transactional
  public void testGetAllSucceedsWithSuperuser() {
    List<AdminUser> result =
        adminUserExtService.getAll(AdminUser.builder().superuser(true).build());
    Assertions.assertNotNull(result);
  }

  @Test
  @Transactional
  public void testGetWithNoPortalFailsForNonSuperuser() {
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminUserExtService.get(
                UUID.randomUUID(), null, AdminUser.builder().superuser(false).build()));
  }

  @Test
  @Transactional
  public void testGetWithPortalAuthsPortal(TestInfo testInfo) {
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    Portal otherPortal = portalFactory.buildPersisted(getTestName(testInfo));
    AdminUserBundle adminUserBundle =
        portalAdminUserFactory.buildPersistedWithPortals(getTestName(testInfo), List.of(portal));
    AdminUserBundle otherAdminUserBundle =
        portalAdminUserFactory.buildPersistedWithPortals(
            getTestName(testInfo), List.of(otherPortal));

    // a portal user can access their own record
    assertThat(
        adminUserExtService
            .get(adminUserBundle.user().getId(), portal.getShortcode(), adminUserBundle.user())
            .getId(),
        equalTo(adminUserBundle.user().getId()));

    // one portal user can't access a user from another portal
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            adminUserExtService.get(
                otherAdminUserBundle.user().getId(),
                otherPortal.getShortcode(),
                adminUserBundle.user()));

    // one portal user can't access a user from another portal if they specify their own portal
    Assertions.assertThrows(
        NotFoundException.class,
        () ->
            adminUserExtService.get(
                otherAdminUserBundle.user().getId(),
                portal.getShortcode(),
                adminUserBundle.user()));
  }

  @Test
  @Transactional
  public void testGetAttachesRolesAndPermissions(TestInfo testInfo) {
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    AdminUserBundle adminUserBundle =
        portalAdminUserFactory.buildPersistedWithPortals(getTestName(testInfo), List.of(portal));
    portalAdminUserRoleService.setRoles(
        adminUserBundle.portalAdminUsers().get(0).getId(),
        List.of("study_admin", "prototype_tester"));
    AdminUser fetchedUser =
        adminUserExtService.get(
            adminUserBundle.user().getId(), portal.getShortcode(), adminUserBundle.user());
    assertThat(fetchedUser.getPortalAdminUsers().size(), equalTo(1));
    PortalAdminUser paUser = fetchedUser.getPortalAdminUsers().get(0);
    assertThat(paUser.getRoles(), hasSize(2));
    Role adminRole =
        paUser.getRoles().stream()
            .filter(role -> role.getName().equals("study_admin"))
            .findFirst()
            .get();
    assertThat(adminRole.getPermissions().size(), greaterThan(0));
  }

  @Test
  @Transactional
  public void testCreateSuperuserFailsForRegularUser() {
    AdminUserExtService.NewAdminUser userToCreate =
        new AdminUserExtService.NewAdminUser("foo", true, null);
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminUserExtService.create(userToCreate, AdminUser.builder().superuser(false).build()));
  }
}
