package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRolesThrowsWhenRoleNotFound");

        assertThrows(RoleNotFoundException.class, () -> {
            portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("unknown"));
        });
    }

    @Transactional
    @Test
    public void testSetRolesReturnsEffectiveRoles() {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRoles");
        Role role1 = roleService.create(Role.builder().name("one").build());
        Role role2 = roleService.create(Role.builder().name("two").build());

        List<String> savedAdminUserRoles = portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("one", "two"));

        PortalAdminUserRole portalAdminUserRole1 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(role1.getId()).build();
        PortalAdminUserRole portalAdminUserRole2 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(role2.getId()).build();
        assertThat(savedAdminUserRoles, containsInAnyOrder("one", "two"));
    }

    @Transactional
    @Test
    public void testSetRolesSavesRoles() {
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRoles");
        Role role1 = roleService.create(Role.builder().name("one").build());
        Role role2 = roleService.create(Role.builder().name("two").build());

        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("one", "two"));

        List<PortalAdminUserRole> rolesForAdminUser = portalAdminUserRoleService.getRolesForAdminUser(portalAdminUser.getId());
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
        PortalAdminUser portalAdminUser = portalAdminUserFactory.buildPersisted("testSetRoles");
        roleService.create(Role.builder().name("old").build());
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("old"));
        Role newRole = roleService.create(Role.builder().name("new").build());

        List<String> updatedAdminUserRoles = portalAdminUserRoleService.setRoles(portalAdminUser.getId(), List.of("new"));

        PortalAdminUserRole portalAdminUserRole2 = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUser.getId()).roleId(newRole.getId()).build();
        assertThat(updatedAdminUserRoles, containsInAnyOrder("new"));

        List<PortalAdminUserRole> updatedRolesForAdminUser = portalAdminUserRoleService.getRolesForAdminUser(portalAdminUser.getId());
        assertThat(updatedRolesForAdminUser, containsInAnyOrder(matchingPortalAdminUserRole(portalAdminUserRole2)));
    }
}
