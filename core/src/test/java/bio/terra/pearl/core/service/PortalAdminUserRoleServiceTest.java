package bio.terra.pearl.core.service;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalAdminUserFactory;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.RoleService;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PortalAdminUserRoleServiceTest extends BaseSpringBootTest {

    @Autowired
    private PortalAdminUserRoleService portalAdminUserRoleService;

    @Autowired
    private PortalAdminUserFactory portalAdminUserFactory;

    @Autowired
    private RoleService roleService;

    private Matcher<PortalAdminUserRole> matchingPortalAdminUserRole(PortalAdminUserRole portalAdminUserRole) {
        return samePropertyValuesAs(portalAdminUserRole, "id", "createdAt", "lastUpdatedAt");
    }

    @Transactional
    @Test
    public void testSetRolesThrowsWhenUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            portalAdminUserRoleService.setRoles(UUID.randomUUID(), List.of());
        });
    }

    @Transactional
    @Test
    public void testSetRolesThrowsWhenRoleNotFound() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRolesThrowsWhenRoleNotFound");

        assertThrows(RoleNotFoundException.class, () -> {
            portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("unknown"));
        });
    }

    @Transactional
    @Test
    public void testSetRolesReturnsEffectiveRoles() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRoles");
        var role1 = roleService.create(Role.builder().name("one").build());
        var role2 = roleService.create(Role.builder().name("two").build());

        var savedAdminUserRoles = portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("one", "two"));

        PortalAdminUserRole portalAdminUserRole1 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(role1.getId()).build();
        PortalAdminUserRole portalAdminUserRole2 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(role2.getId()).build();
        assertThat(savedAdminUserRoles, containsInAnyOrder("one", "two"));
    }

    @Transactional
    @Test
    public void testSetRolesSavesRoles() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRoles");
        var role1 = roleService.create(Role.builder().name("one").build());
        var role2 = roleService.create(Role.builder().name("two").build());

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("one", "two"));

        var rolesForAdminUser = portalAdminUserRoleService.getRolesForAdminUser(portalAdminUser.getId());
        PortalAdminUserRole portalAdminUserRole1 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(role1.getId()).build();
        PortalAdminUserRole portalAdminUserRole2 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(role2.getId()).build();
        assertThat(rolesForAdminUser, containsInAnyOrder(
                matchingPortalAdminUserRole(portalAdminUserRole1),
                matchingPortalAdminUserRole(portalAdminUserRole2)
        ));
    }

    @Transactional
    @Test
    public void testSetRolesReplacesExistingRoles() {
        var portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRoles");
        roleService.create(Role.builder().name("old").build());
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("old"));
        var newRole = roleService.create(Role.builder().name("new").build());

        var updatedAdminUserRoles = portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("new"));

        PortalAdminUserRole portalAdminUserRole2 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(newRole.getId()).build();
        assertThat(updatedAdminUserRoles, containsInAnyOrder("new"));

        var updatedRolesForAdminUser = portalAdminUserRoleService.getRolesForAdminUser(portalAdminUser.getId());
        assertThat(updatedRolesForAdminUser, containsInAnyOrder(matchingPortalAdminUserRole(portalAdminUserRole2)));
    }
}
