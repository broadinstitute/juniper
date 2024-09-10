package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.admin.RolePermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    public void testAddRole(TestInfo info) {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted(getTestName(info));
        String roleName = "testAddRole.role";
        Role role = roleFactory.buildPersisted(roleName);

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName), getAuditInfo(info));

        assertThat(portalAdminUserFactory.userHasRole(portalAdminUser.getId(), roleName), equalTo(true));
    }

    @Transactional
    @Test
    public void testAdminUserWithMultipleRoles(TestInfo info) {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted(getTestName(info));
        String roleName1 = getTestName(info) + ".role1";
        String roleName2 = getTestName(info) + ".role2";
        Role role1 = roleFactory.buildPersisted(roleName1);
        Role role2 = roleFactory.buildPersisted(roleName2);

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName1, roleName2), getAuditInfo(info));

        assertThat(portalAdminUserFactory.userHasRole(portalAdminUser.getId(), roleName1), equalTo(true));
        assertThat(portalAdminUserFactory.userHasRole(portalAdminUser.getId(), roleName2), equalTo(true));
    }

    @Transactional
    @Test
    public void testRemoveRole(TestInfo info) {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted(getTestName(info));
        String roleName = getTestName(info) + ".role";
        Role role = roleFactory.buildPersisted(roleName);
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName), getAuditInfo(info));

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(), getAuditInfo(info));

        assertThat(portalAdminUserFactory.userHasRole(portalAdminUser.getId(), roleName), equalTo(false));
    }

    @Transactional
    @Test
    public void testAddRemovePermission(TestInfo info) {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted(getTestName(info));
        String roleName = getTestName(info) + ".role";
        Role role = roleFactory.buildPersisted(roleName);
        String permissionName = getTestName(info) + "permission";
        Permission permission = permissionFactory.buildPersisted(permissionName);
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(roleName), getAuditInfo(info));

        assertThat(portalAdminUserFactory.userHasPermission(portalAdminUser.getId(), permissionName), equalTo(false));

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermissionId(permission.getId());
        rolePermissionService.create(rolePermission);

        assertThat(portalAdminUserFactory.userHasPermission(portalAdminUser.getId(), permissionName), equalTo(true));
    }
}
