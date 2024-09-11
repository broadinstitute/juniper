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
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
    public void testCrud(TestInfo info) {
        AdminUser user = adminUserFactory.builder(getTestName(info)).build();
        AdminUser savedUser = adminUserService.create(user, getAuditInfo(info));
        DaoTestUtils.assertGeneratedProperties(savedUser);

        Optional<AdminUser> foundUser = adminUserService.findByUsername(user.getUsername());
        assertThat(foundUser.get().getId(), equalTo(savedUser.getId()));

        adminUserService.delete(savedUser.getId(), getAuditInfo(info), CascadeProperty.EMPTY_SET);
        assertThat(adminUserService.findByUsername(user.getUsername()).isEmpty(), equalTo(true));
    }

    @Test
    @Transactional
    public void testCaseInsensitiveLookup(TestInfo info) {
        AdminUser user = adminUserFactory.builder(getTestName(info))
                .username("MixedCaseName@test.co" + RandomStringUtils.randomNumeric(3))
                .build();
        adminUserService.create(user, getAuditInfo(info));

        assertThat(adminUserService.findByUsername(user.getUsername()).isPresent(), is(true));
        assertThat(adminUserService.findByUsername(user.getUsername().toUpperCase()).isPresent(), is(true));
        assertThat(adminUserService.findByUsername(user.getUsername().toLowerCase()).isPresent(), is(true));
        assertThat(adminUserService.findByUsername(user.getUsername() + "x").isPresent(), is(false));
    }

    @Test
    @Transactional
    public void testLoadWithPermissions(TestInfo info) {
        Permission permission1 = permissionFactory.buildPersisted(getTestName(info) + "1");
        Permission permission2 = permissionFactory.buildPersisted(getTestName(info) + "2");
        Role role1 = roleFactory.buildPersisted(getTestName(info), List.of(permission1.getName(), permission2.getName()));
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted(getTestName(info));
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(role1.getName()), getAuditInfo(info));

        AdminUser adminUser = adminUserService.find(portalAdminUser.getAdminUserId()).get();
        AdminUserWithPermissions loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();

        assertThat(loadedUser.user().getId(), equalTo(adminUser.getId()));
        assertThat(loadedUser.portalPermissions().values(), hasSize(1));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(2));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasItems(
                permission1.getName(), permission2.getName()
        ) );

        // does not include permissions/roles not given to the user
        Permission permission3 = permissionFactory.buildPersisted(getTestName(info) + "3");
        Role role2 = roleFactory.buildPersisted(getTestName(info) + "2",
                List.of(permission1.getName(), permission3.getName()));
        loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(2));

        // includes across multiple roles, and doesn't duplicate permissions
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of(role1.getName(), role2.getName()), getAuditInfo(info));
        loadedUser = adminUserService.findByUsernameWithPermissions(adminUser.getUsername()).get();
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasSize(3));
        assertThat(loadedUser.portalPermissions().get(portalAdminUser.getPortalId()), hasItems(
                permission1.getName(), permission2.getName(), permission3.getName()
        ) );
    }

    @Test
    @Transactional
    public void testGetByPortal(TestInfo info) {
        AdminUser savedPortalUser = adminUserService.create(adminUserFactory.builder(getTestName(info)).build(), getAuditInfo(info));
        AdminUser savedNonPortalUser = adminUserService.create(adminUserFactory.builder(getTestName(info)).build(), getAuditInfo(info));
        AdminUser savedOtherPortalUser = adminUserService.create(adminUserFactory.builder(getTestName(info)).build(), getAuditInfo(info));
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        Portal otherPortal = portalFactory.buildPersisted(getTestName(info));
        Portal emptyPortal = portalFactory.buildPersisted(getTestName(info));
        Role role = roleFactory.buildPersisted(getTestName(info));

        PortalAdminUser savedPortalAdminUser = portalAdminUserService.create(PortalAdminUser.builder()
            .adminUserId(savedPortalUser.getId())
            .portalId(portal.getId()).build(), getAuditInfo(info));
        portalAdminUserService.create(PortalAdminUser.builder()
            .adminUserId(savedOtherPortalUser.getId())
            .portalId(otherPortal.getId()).build(), getAuditInfo(info));
        portalAdminUserRoleService.create(PortalAdminUserRole.builder()
            .portalAdminUserId(savedPortalAdminUser.getId())
            .roleId(role.getId()).build(), getAuditInfo(info));

        List<AdminUser> users = adminUserService.findAllWithRolesByPortal(portal.getId());
        assertThat(users, hasSize(1));
        assertThat(users.get(0).getId(), equalTo(savedPortalUser.getId()));
        assertThat(users.get(0).getPortalAdminUsers(), hasSize(1));

        List<AdminUser> emptyPortalUsers = adminUserService.findAllWithRolesByPortal(emptyPortal.getId());
        assertThat(emptyPortalUsers, hasSize(0));
    }

    @Test
    @Transactional
    public void testAdminUserMoreThanOnePortal(TestInfo info) {
        Portal portal1 = portalFactory.buildPersisted(getTestName(info));
        Portal portal2 = portalFactory.buildPersisted(getTestName(info));

        AdminUser adminUser = adminUserFactory.builder(getTestName(info)).build();

        //Add the admin user to the first portal
        adminUser.setPortalAdminUsers(List.of(PortalAdminUser.builder().portalId(portal1.getId()).build()));
        adminUserService.create(adminUser, getAuditInfo(info));

        //Add the admin user to the second portal
        adminUser.setPortalAdminUsers(List.of(PortalAdminUser.builder().portalId(portal2.getId()).build()));
        adminUserService.create(adminUser, getAuditInfo(info));

        List<AdminUser> adminUsersList = adminUserService.findAllWithRoles();
        List<UUID> portalIds = adminUsersList.stream()
                .filter(user -> user.getUsername().equals(adminUser.getUsername()))
                .findFirst().get()
                .getPortalAdminUsers().stream().map(PortalAdminUser::getPortalId).toList();
        assertThat(portalIds, containsInAnyOrder(portal1.getId(), portal2.getId()));
    }
}
