package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.Portal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PortalAdminUserServiceTest extends BaseSpringBootTest {

    @Autowired
    private PortalAdminUserFactory portalAdminUserFactory;
    @Autowired
    private PortalAdminUserRoleService portalAdminUserRoleService;
    @Autowired
    private PortalAdminUserService portalAdminUserService;
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private RoleService roleService;


    @Transactional
    @Test
    public void testRemoveUserFromPortal(TestInfo info) {
        // set up a user in two portals
        Portal portal1 = portalFactory.buildPersisted(getTestName(info));
        Portal portal2 = portalFactory.buildPersisted(getTestName(info));
        List<PortalAdminUser> portalUsers =
                portalAdminUserFactory.buildPersistedWithPortals(getTestName(info), List.of(portal1, portal2)).portalAdminUsers();
        PortalAdminUser user1Portal1 = portalUsers.get(0);
        PortalAdminUser user1Portal2 = portalUsers.get(1);

        // add roles for user in each portal
        roleService.create(Role.builder().name("role1").build());
        roleService.create(Role.builder().name("role2").build());
        DataAuditInfo auditInfo = getAuditInfo(info);
        portalAdminUserRoleService.setRoles(user1Portal1.getId(), List.of("role1", "role2"), auditInfo);
        portalAdminUserRoleService.setRoles(user1Portal2.getId(), List.of("role2"), auditInfo);

        portalAdminUserService.delete(user1Portal1.getId(), auditInfo);

        // confirm user is gone in proper portal
        List<PortalAdminUser> users = portalAdminUserService.findByPortal(portal1.getId());
        assertThat(users.size(), equalTo(0));

        List<PortalAdminUser> portal2Users = portalAdminUserService.findByPortal(portal2.getId());
        assertThat(portal2Users.size(), equalTo(1));
        PortalAdminUser portal2user = portal2Users.get(0);
        assertThat(portal2user.getAdminUserId(), equalTo(user1Portal2.getAdminUserId()));
        assertTrue(portalAdminUserFactory.userHasRole(portal2user.getId(), "role2"));
    }

    @Transactional
    @Test
    public void testDeleteByUserId(TestInfo info) {
        String testName = "testDeleteByUserId";
        // set up a user in two portals and another in one portal
        Portal portal1 = portalFactory.buildPersisted(testName);
        Portal portal2 = portalFactory.buildPersisted(testName);
        List<PortalAdminUser> portalUsers1 =
                portalAdminUserFactory.buildPersistedWithPortals(testName, List.of(portal1, portal2)).portalAdminUsers();
        PortalAdminUser user1Portal1 = portalUsers1.get(0);
        PortalAdminUser user1Portal2 = portalUsers1.get(1);

        List<PortalAdminUser> portalUsers2 = portalAdminUserFactory.buildPersistedWithPortals(testName, List.of(portal1)).portalAdminUsers();
        PortalAdminUser user2 = portalUsers2.get(0);

        // add roles for all
        roleService.create(Role.builder().name("role1").build());
        roleService.create(Role.builder().name("role2").build());
        DataAuditInfo auditInfo = getAuditInfo(info);
        portalAdminUserRoleService.setRoles(user1Portal1.getId(), List.of("role1", "role2"), auditInfo);
        portalAdminUserRoleService.setRoles(user1Portal2.getId(), List.of("role2"), auditInfo);
        portalAdminUserRoleService.setRoles(user2.getId(), List.of("role1"), auditInfo);

        portalAdminUserService.deleteByUserId(user1Portal1.getAdminUserId(), auditInfo);

        // confirm user1 is gone in both portals and user2 is unchanged
        List<PortalAdminUser> users = portalAdminUserService.findByPortal(portal1.getId());
        assertThat(users.size(), equalTo(1));
        PortalAdminUser foundUser = users.get(0);
        assertThat(foundUser.getAdminUserId(), equalTo(user2.getAdminUserId()));

        assertTrue(portalAdminUserFactory.userHasRole(foundUser.getId(), "role1"));

        List<PortalAdminUser> portal2Users = portalAdminUserService.findByPortal(portal2.getId());
        assertThat(portal2Users.size(), equalTo(0));
    }
}
