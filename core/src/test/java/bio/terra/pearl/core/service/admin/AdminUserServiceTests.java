package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.admin.PermissionFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.admin.RoleFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.CascadeProperty;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.apache.commons.lang3.RandomStringUtils;
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
    @Autowired
    private PortalAdminUserService portalAdminUserService;
    @Autowired
    private PortalFactory portalFactory;

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
    public void testCaseInsensitiveLookup() {
        AdminUser user = adminUserFactory.builder("testAdminUserCrud")
                .username("MixedCaseName@test.co" + RandomStringUtils.randomNumeric(3))
                .build();
        adminUserService.create(user);

        assertThat(adminUserService.findByUsername(user.getUsername()).isPresent(), is(true));
        assertThat(adminUserService.findByUsername(user.getUsername().toUpperCase()).isPresent(), is(true));
        assertThat(adminUserService.findByUsername(user.getUsername().toLowerCase()).isPresent(), is(true));
        assertThat(adminUserService.findByUsername(user.getUsername() + "x").isPresent(), is(false));
    }

    @Test
    @Transactional
    public void testLoadWithPermissions() {
        Permission permission1 = permissionFactory.buildPersisted("testLoadWithPerms.perm1");
        Permission permission2 = permissionFactory.buildPersisted("testLoadWithPerms.perm2");
        Role role1 = roleFactory.buildPersisted("testLoadWithPerms", List.of("testLoadWithPerms.perm1", "testLoadWithPerms.perm2"));
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testLoadWithPermissions");
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(role1.getName()));

        AdminUser adminUser = adminUserService.find(portalAdminUser.getAdminUserId()).get();
        AdminUserWithPermissions loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();

        assertThat(loadedUser.user().getId(), equalTo(adminUser.getId()));
        assertThat(loadedUser.portalPermissions().values(), hasSize(1));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(2));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasItems(
                "testLoadWithPerms.perm1", "testLoadWithPerms.perm2"
        ) );

        // does not include permissions/roles not given to the user
        Permission permission3 = permissionFactory.buildPersisted("testLoadWithPerms.perm3");
        Role role2 = roleFactory.buildPersisted("testLoadWithPermsRole2", List.of("testLoadWithPerms.perm1", "testLoadWithPerms.perm3"));
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

    @Test
    @Transactional
    public void testGetByPortal() {
        AdminUser savedPortalUser = adminUserService.create(adminUserFactory.builder("testGetByPortal").build());
        AdminUser savedNonPortalUser = adminUserService.create(adminUserFactory.builder("testGetByPortal").build());
        AdminUser savedOtherPortalUser = adminUserService.create(adminUserFactory.builder("testGetByPortal").build());
        Portal portal = portalFactory.buildPersisted("testGetByPortal");
        Portal otherPortal = portalFactory.buildPersisted("testGetByPortal");
        Portal emptyPortal = portalFactory.buildPersisted("testGetByPortal");
        Role role = roleFactory.buildPersisted("testGetByPortal");

        PortalAdminUser savedPortalAdminUser = portalAdminUserService.create(PortalAdminUser.builder()
            .adminUserId(savedPortalUser.getId())
            .portalId(portal.getId()).build());
        portalAdminUserService.create(PortalAdminUser.builder()
            .adminUserId(savedOtherPortalUser.getId())
            .portalId(otherPortal.getId()).build());
        portalAdminUserRoleService.create(PortalAdminUserRole.builder()
            .portalAdminUserId(savedPortalAdminUser.getId())
            .roleId(role.getId()).build());

        List<AdminUser> users = adminUserService.findAllWithRolesByPortal(portal.getId());
        assertThat(users, hasSize(1));
        assertThat(users.get(0).getId(), equalTo(savedPortalUser.getId()));
        assertThat(users.get(0).getPortalAdminUsers(), hasSize(1));

        List<AdminUser> emptyPortalUsers = adminUserService.findAllWithRolesByPortal(emptyPortal.getId());
        assertThat(emptyPortalUsers, hasSize(0));
    }
}
