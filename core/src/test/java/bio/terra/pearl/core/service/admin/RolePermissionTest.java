package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.admin.RolePermission;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testAddRole");
        String roleName = "testAddRole.role";
        Role role = roleFactory.buildPersisted(roleName);

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName));

        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName), equalTo(true));
    }

    @Transactional
    @Test
    public void testAdminUserWithMultipleRoles() {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testAdminUserWithMultipleRoles");
        String roleName1 = "testAdminUserWithMultipleRoles.role1";
        String roleName2 = "testAdminUserWithMultipleRoles.role2";
        Role role1 = roleFactory.buildPersisted(roleName1);
        Role role2 = roleFactory.buildPersisted(roleName2);

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName1, roleName2));

        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName1), equalTo(true));
        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName2), equalTo(true));
    }

    @Transactional
    @Test
    public void testRemoveRole() {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testRemoveRole");
        String roleName = "testRemoveRole.role";
        Role role = roleFactory.buildPersisted(roleName);
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName));

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of());

        assertThat(portalAdminUserService.userHasRole(portalAdminUser.getId(), roleName), equalTo(false));
    }

    @Transactional
    @Test
    public void testAddRemovePermission() {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testUserHasPermission");
        String roleName = "testUserHasPermission.role";
        Role role = roleFactory.buildPersisted(roleName);
        String permissionName = "testUserHasPermission.permission";
        Permission permission = permissionFactory.buildPersisted(permissionName);
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName));

        assertThat(portalAdminUserService.userHasPermission(portalAdminUser.getId(), permissionName), equalTo(false));

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermissionId(permission.getId());
        rolePermissionService.create(rolePermission);

        assertThat(portalAdminUserService.userHasPermission(portalAdminUser.getId(), permissionName), equalTo(true));

        // TODO: remove permission
    }
}
