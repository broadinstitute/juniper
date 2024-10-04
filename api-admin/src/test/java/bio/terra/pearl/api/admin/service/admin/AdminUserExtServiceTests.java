package bio.terra.pearl.api.admin.service.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.factory.admin.AdminUserBundle;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import java.util.List;
import java.util.Map;
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
  @Autowired private RoleFactory roleFactory;
  @Autowired private PortalAdminUserService portalAdminUserService;

  @Test
  public void testAllMethodsAnnotated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        adminUserExtService,
        Map.of(
            "get",
            AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "getInPortal",
            AuthAnnotationSpec.withPortalPerm("BASE"),
            "getAll",
            AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "findByPortal",
            AuthAnnotationSpec.withPortalPerm("BASE"),
            "createSuperuser",
            AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "createAdminUser",
            AuthAnnotationSpec.withPortalPerm("admin_user_edit"),
            "delete",
            AuthAnnotationSpec.withOtherAnnotations(List.of(SuperuserOnly.class)),
            "deleteInPortal",
            AuthAnnotationSpec.withPortalPerm("admin_user_edit"),
            "setPortalUserRoles",
            AuthAnnotationSpec.withPortalPerm("admin_user_edit")));
  }

  @Test
  @Transactional
  public void testCreateNewAdminAndPortalUser(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    roleFactory.buildPersistedCreatePermissions("study_admin", List.of("admin_user_edit"));
    AdminUserBundle operatorBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal, List.of("study_admin"));

    AdminUser createdUser =
        adminUserExtService.createAdminUser(
            PortalAuthContext.of(operatorBundle.user(), portal.getShortcode()),
            "newGuy@new.com",
            List.of("study_admin"));

    assertThat(
        portalAdminUserService
            .findByUserIdAndPortal(createdUser.getId(), portal.getId())
            .isPresent(),
        equalTo(true));
  }

  @Test
  @Transactional
  public void testCreateNewPortalUser(TestInfo info) {
    /** add a user already in another portal to a new portal */
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Portal portal2 = portalFactory.buildPersisted(getTestName(info));
    roleFactory.buildPersistedCreatePermissions("study_admin", List.of("admin_user_edit"));
    AdminUserBundle operatorBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal, List.of("study_admin"));

    AdminUserBundle userBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal2, List.of("study_admin"));

    AdminUser createdUser =
        adminUserExtService.createAdminUser(
            PortalAuthContext.of(operatorBundle.user(), portal.getShortcode()),
            userBundle.user().getUsername(),
            List.of("study_admin"));

    assertThat(
        portalAdminUserService
            .findByUserIdAndPortal(createdUser.getId(), portal2.getId())
            .isPresent(),
        equalTo(true));
  }

  @Test
  @Transactional
  public void testGetAttachesRolesAndPermissions(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    roleFactory.buildPersistedCreatePermissions("study_admin", List.of("admin_user_edit"));
    roleFactory.buildPersisted("prototype_tester");
    AdminUserBundle adminUserBundle =
        portalAdminUserFactory.buildPersistedWithPortals(getTestName(info), List.of(portal));
    portalAdminUserRoleService.setRoles(
        adminUserBundle.portalAdminUsers().get(0).getId(),
        List.of("study_admin", "prototype_tester"),
        getAuditInfo(info));
    AdminUser fetchedUser =
        adminUserExtService.getInPortal(
            PortalAuthContext.of(adminUserBundle.user(), portal.getShortcode()),
            adminUserBundle.user().getId());
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
  public void testSetRolesLimitedByOperator(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Role role1 = roleFactory.buildPersisted(getTestName(info));
    roleFactory.buildPersistedCreatePermissions("study_admin", List.of("admin_user_edit"));

    AdminUserBundle operatorBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal, List.of("study_admin"));

    AdminUserBundle userBundle =
        portalAdminUserFactory.buildPersistedWithRoles(getTestName(info), portal, List.of());

    adminUserExtService.setPortalUserRoles(
        PortalAuthContext.of(operatorBundle.user(), portal.getShortcode()),
        userBundle.user().getId(),
        List.of("study_admin"));
    assertThat(
        portalAdminUserFactory.userHasRole(
            userBundle.portalAdminUsers().get(0).getId(), role1.getName()),
        is(false));
    assertThat(
        portalAdminUserFactory.userHasRole(
            userBundle.portalAdminUsers().get(0).getId(), "study_admin"),
        is(true));

    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminUserExtService.setPortalUserRoles(
                PortalAuthContext.of(operatorBundle.user(), portal.getShortcode()),
                userBundle.user().getId(),
                List.of(role1.getName())));

    assertThat(
        portalAdminUserFactory.userHasRole(
            userBundle.portalAdminUsers().get(0).getId(), role1.getName()),
        is(false));

    // it's ok to include roles the target user already has,
  }

  @Test
  @Transactional
  public void testSetRolesAllowsExistingOperator(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Role role1 = roleFactory.buildPersisted(getTestName(info));
    roleFactory.buildPersistedCreatePermissions("study_admin", List.of("admin_user_edit"));

    AdminUserBundle operatorBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal, List.of("study_admin"));

    AdminUserBundle userBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal, List.of(role1.getName()));

    adminUserExtService.setPortalUserRoles(
        PortalAuthContext.of(operatorBundle.user(), portal.getShortcode()),
        userBundle.user().getId(),
        List.of("study_admin", role1.getName()));

    assertThat(
        portalAdminUserFactory.userHasRole(
            userBundle.portalAdminUsers().get(0).getId(), "study_admin"),
        is(true));
  }

  /** Important enough that it's worth a separate test beyond the annotation check */
  @Test
  @Transactional
  public void testCreateSuperuserFailsForRegularUser() {
    AdminUser operator = AdminUser.builder().superuser(false).build();
    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            adminUserExtService.createSuperuser(
                OperatorAuthContext.of(operator), "someone@scam.crime"));
  }
}
