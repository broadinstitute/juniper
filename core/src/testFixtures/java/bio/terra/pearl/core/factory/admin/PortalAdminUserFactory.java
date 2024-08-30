package bio.terra.pearl.core.factory.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortalAdminUserFactory {

    @Autowired
    private AdminUserFactory adminUserFactory;

    @Autowired
    private PortalAdminUserService portalAdminUserService;
    @Autowired
    private PortalAdminUserRoleService portalAdminUserRoleService;

    @Autowired
    private PortalFactory portalFactory;

    public PortalAdminUser.PortalAdminUserBuilder builder(String testName) {
        return PortalAdminUser.builder();
    }

    public PortalAdminUser.PortalAdminUserBuilder builderWithDependencies(String testName) {
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        Portal portal = portalFactory.buildPersisted(testName);
        return PortalAdminUser.builder().adminUserId(adminUser.getId()).portalId(portal.getId());
    }

    public PortalAdminUser buildPersisted(String testName) {
        PortalAdminUser portalAdminUser = builderWithDependencies(testName).build();
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(testName).build();
        return portalAdminUserService.create(portalAdminUser, auditInfo);
    }

    public PortalAdminUser buildPersisted(String testName, UUID adminUserId, UUID portalId) {
        PortalAdminUser portalAdminUser = PortalAdminUser.builder()
                .adminUserId(adminUserId).portalId(portalId).build();
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(testName).build();
        return portalAdminUserService.create(portalAdminUser, auditInfo);
    }

    public AdminUserBundle buildPersistedWithPortals(String testName, List<Portal> portals) {
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        List<PortalAdminUser> portalUsers = new ArrayList<>();
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(testName).build();
        for (Portal portal : portals) {
            portalUsers.add(portalAdminUserService.create(PortalAdminUser.builder()
                    .adminUserId(adminUser.getId())
                    .portalId(portal.getId())
                    .build(), auditInfo));
        }
        return new AdminUserBundle(adminUser, portalUsers);
    }

    public AdminUserBundle buildPersistedWithRoles(String testName, Portal portal, List<String> roleNames) {
        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
        List<PortalAdminUser> portalUsers = new ArrayList<>();
        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(testName).build();
        portalUsers.add(portalAdminUserService.create(PortalAdminUser.builder()
                    .adminUserId(adminUser.getId())
                    .portalId(portal.getId())
                    .build(), auditInfo));
        portalAdminUserRoleService.setRoles(portalUsers.get(0).getId(), roleNames, auditInfo);
        return new AdminUserBundle(adminUser, portalUsers);
    }

//    public AdminUserBundle buildPersistedWithPermissions(String testName, Portal portal, List<String> permissionNames) {
//        AdminUser adminUser = adminUserFactory.buildPersisted(testName);
//        List<PortalAdminUser> portalUsers = new ArrayList<>();
//        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(testName).build();
//        portalUsers.add(portalAdminUserService.create(PortalAdminUser.builder()
//                .adminUserId(adminUser.getId())
//                .portalId(portal.getId())
//                .build(), auditInfo));
//        portalAdminUserRoleService.setRoles(portalUsers.get(0).getId(), roleNames, auditInfo);
//        return new AdminUserBundle(adminUser, portalUsers);
//    }

    /** brute force role check done by reloading the whole user */
    public boolean userHasRole(UUID portalAdminUserId, String roleName) {
        PortalAdminUser portalAdminUser = portalAdminUserService.findWithRolesAndPermissions(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        return portalAdminUser.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }

    /** brute force permission check done by reloading the whole user */
    public boolean userHasPermission(UUID portalAdminUserId, String permissionName) {
        PortalAdminUser portalAdminUser = portalAdminUserService.findWithRolesAndPermissions(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        List<Permission> permissions = portalAdminUser.getRoles().stream().flatMap(role -> role.getPermissions().stream()).toList();

        return permissions.stream().anyMatch(role -> role.getName().equals(permissionName));
    }
}
