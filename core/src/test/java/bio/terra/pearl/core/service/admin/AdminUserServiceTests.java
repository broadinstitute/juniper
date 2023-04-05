package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.CascadeProperty;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AdminUserServiceTests extends BaseSpringBootTest {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private PortalAdminUserRoleService portalAdminUserRoleService;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private PermissionFactory permissionFactory;
    @Autowired
    private RoleFactory roleFactory;
    @Autowired
    private PortalAdminUserFactory portalAdminUserFactory;

    @Test
    @Transactional
    public void testCrud() {
        AdminUser user = adminUserFactory.builder("testAdminUserCrud").build();
        AdminUser savedUser = adminUserService.create(user);
        DaoTestUtils.assertGeneratedProperties(savedUser);

        Optional<AdminUser> foundUser = adminUserService.findByUsername(user.getUsername());
        assertThat(foundUser.get().getId(), equalTo(savedUser.getId()));

        adminUserService.delete(savedUser.getId(), CascadeProperty.EMPTY_SET);
        assertThat(adminUserService.findByUsername(user.getUsername()).isEmpty(), equalTo(true));
    }

    @Test
    @Transactional
    public void testLoadWithPermissions() {
        var permission1 = permissionFactory.buildPersisted("testLoadWithPerms.perm1");
        var permission2 = permissionFactory.buildPersisted("testLoadWithPerms.perm2");
        var role1 = roleFactory.buildPersisted("testLoadWithPerms", List.of("testLoadWithPerms.perm1", "testLoadWithPerms.perm2"));
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testLoadWithPermissions");
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(role1.getName()));

        AdminUser adminUser = adminUserService.find(portalAdminUser.getAdminUserId()).get();
        var loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();

        assertThat(loadedUser.user().getId(), equalTo(adminUser.getId()));
        assertThat(loadedUser.portalPermissions().values(), hasSize(1));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(2));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasItems(
                "testLoadWithPerms.perm1", "testLoadWithPerms.perm2"
        ) );

        // does not include permissions/roles not given to the user
        var permission3 = permissionFactory.buildPersisted("testLoadWithPerms.perm3");
        var role2 = roleFactory.buildPersisted("testLoadWithPermsRole2", List.of("testLoadWithPerms.perm1", "testLoadWithPerms.perm3"));
        loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(2));

        // includes across multiple roles, and doesn't duplicate permissions
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(role1.getName(), role2.getName()));
        loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(3));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasItems(
                "testLoadWithPerms.perm1", "testLoadWithPerms.perm2", "testLoadWithPerms.perm3"
        ) );

    }
}
