package bio.terra.pearl.core.service;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.model.admin.RolePermission;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.admin.RolePermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RolePermissionTest extends BaseSpringBootTest {

    @Autowired
    private PermissionFactory permissionFactory;

    @Autowired
    private PortalAdminUserFactory portalAdminUserFactory;

    @Autowired
    private PortalAdminUserService portalAdminUserService;

    @Autowired
    private PortalAdminUserRoleService portalAdminUserRoleService;

    @Autowired
    private RoleFactory roleFactory;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Transactional
    @Test
    public void testAddRole() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testAddRole");
        var roleName = "testAddRole.role";
        var role = roleFactory.buildPersisted(roleName);

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName));

        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName), equalTo(true));
    }

    @Transactional
    @Test
    public void testAdminUserWithMultipleRoles() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testAdminUserWithMultipleRoles");
        var roleName1 = "testAdminUserWithMultipleRoles.role1";
        var roleName2 = "testAdminUserWithMultipleRoles.role2";
        var role1 = roleFactory.buildPersisted(roleName1);
        var role2 = roleFactory.buildPersisted(roleName2);

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName1, roleName2));

        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName1), equalTo(true));
        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName2), equalTo(true));
    }

    @Transactional
    @Test
    public void testRemoveRole() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testRemoveRole");
        var roleName = "testRemoveRole.role";
        var role = roleFactory.buildPersisted(roleName);
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName));

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of());

        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName), equalTo(false));
    }

    @Transactional
    @Test
    public void testAddRemovePermission() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testUserHasPermission");
        var roleName = "testUserHasPermission.role";
        var role = roleFactory.buildPersisted(roleName);
        var permissionName = "testUserHasPermission.permission";
        var permission = permissionFactory.buildPersisted(permissionName);
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName));

        assertThat(portalAdminUserService.userHasPermission(portalAdminUser.getId(), permissionName), equalTo(false));

        var rolePermission = new RolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermissionId(permission.getId());
        rolePermissionService.create(rolePermission);

        assertThat(portalAdminUserService.userHasPermission(portalAdminUser.getId(), permissionName), equalTo(true));

        // TODO: remove permission
    }
}
