package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.AdminUserWithPermissions;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService extends CrudService<AdminUser, AdminUserDao> {
    private PortalAdminUserService portalAdminUserService;
    private PortalAdminUserRoleService portalAdminUserRoleService;

    public AdminUserService(AdminUserDao adminUserDao, PortalAdminUserService portalAdminUserService,
                            PortalAdminUserRoleService portalAdminUserRoleService) {
        super(adminUserDao);
        this.portalAdminUserService = portalAdminUserService;
        this.portalAdminUserRoleService = portalAdminUserRoleService;
    }

    public Optional<AdminUser> findByUsername(String username) {
        return dao.findByUsername(username);
    }

    public Optional<AdminUserWithPermissions> findByUsernameWithPermissions(String username) {
        return dao.findByUsernameWithPermissions(username);
    }

    @Transactional
    @Override
    public AdminUser create(AdminUser adminUser) {
        //An AdminUser could belong to more than one portal, so we need to check if the user already exists
        //before creating. If the user exists, we'll just add the new portal to the existing user.
        AdminUser savedUser = dao.findByUsername(adminUser.getUsername())
                                 .orElseGet(() -> dao.create(adminUser));

        logger.info("Created AdminUser - id: {}, username: {}", savedUser.getId(), savedUser.getUsername());
        for (PortalAdminUser portalAdminUser : adminUser.getPortalAdminUsers()) {
            portalAdminUser.setAdminUserId(savedUser.getId());
            portalAdminUserService.create(portalAdminUser);
        }
        return savedUser;
    }

    @Override
    @Transactional
    public void delete(UUID adminUserId, Set<CascadeProperty> cascade) {
        portalAdminUserService.deleteByUserId(adminUserId);
        dao.delete(adminUserId);
    }

    public List<AdminUser> findAllWithRoles() {
        List<AdminUser> adminUsers = dao.findAll();
        List<PortalAdminUser> portalAdminUsers = portalAdminUserService.findAll();
        List<PortalAdminUserRole> portalAdminUserRoles = portalAdminUserRoleService.findAll();
        attachRoles(portalAdminUsers, portalAdminUserRoles);
        attachPortalUsers(adminUsers, portalAdminUsers);
        return adminUsers;
    }

    public List<AdminUser> findAllWithRolesByPortal(UUID portalId) {
        List<PortalAdminUser> portalAdminUsers = portalAdminUserService.findByPortal(portalId);
        List<AdminUser> adminUsers = dao.findAll(portalAdminUsers.stream().map(PortalAdminUser::getAdminUserId).toList());
        List<PortalAdminUserRole> portalAdminUserRoles = portalAdminUserRoleService
            .findAllByPortalAdminUserIds(portalAdminUsers.stream().map(PortalAdminUser::getId).toList());
        attachRoles(portalAdminUsers, portalAdminUserRoles);
        attachPortalUsers(adminUsers, portalAdminUsers);
        return adminUsers;
    }

    private void attachPortalUsers(List<AdminUser> adminUsers, List<PortalAdminUser> portalAdminUsers) {
        // map the users by id for quick assigning of portal admins
        Map<UUID, AdminUser> userIdMap = new HashMap<>();
        for (AdminUser user : adminUsers) {
            userIdMap.put(user.getId(), user);
        }
        for (PortalAdminUser portalAdminUser : portalAdminUsers) {
            userIdMap.get(portalAdminUser.getAdminUserId())
                .getPortalAdminUsers().add(portalAdminUser);
        }
    }

    private void attachRoles(List<PortalAdminUser> portalAdminUsers, List<PortalAdminUserRole> portalAdminUserRoles) {
        // map the portalAdmins by id for quick assigning of roles
        Map<UUID, PortalAdminUser> portalUserIdMap = new HashMap<>();
        for (PortalAdminUser portalAdminUser : portalAdminUsers) {
            portalUserIdMap.put(portalAdminUser.getId(), portalAdminUser);
        }
        for (PortalAdminUserRole portalAdminUserRole : portalAdminUserRoles) {
            portalUserIdMap.get(portalAdminUserRole.getPortalAdminUserId())
                .getRoleIds().add(portalAdminUserRole.getRoleId());
        }
    }
}
