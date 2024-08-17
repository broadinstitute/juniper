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
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
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
            AuthAnnotationSpec.withPortalPerm("admin_user_edit")));
  }

  @Test
  @Transactional
  public void testGetAttachesRolesAndPermissions(TestInfo info) {
    Portal portal = portalFactory.buildPersisted(getTestName(info));
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
