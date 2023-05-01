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
        AdminUser savedUser = dao.create(adminUser);
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

    public List<AdminUser> findAllWithPortalsAndRoles() {
        List<AdminUser> adminUsers = dao.findAll();
        List<PortalAdminUser> portalAdminUsers = portalAdminUserService.findAll();
        List<PortalAdminUserRole> portalAdminUserRoles = portalAdminUserRoleService.findAll();

        // map the portalAdmins by id for quick assigning of roles
        Map<UUID, PortalAdminUser> portalUserIdMap = new HashMap<>();
        for (PortalAdminUser portalAdminUser : portalAdminUsers) {
            portalUserIdMap.put(portalAdminUser.getId(), portalAdminUser);
        }
        for (PortalAdminUserRole portalAdminUserRole : portalAdminUserRoles) {
            portalUserIdMap.get(portalAdminUserRole.getPortalAdminUserId())
                .getRoleIds().add(portalAdminUserRole.getRoleId());
        }

        // map the users by id for quick assigning of portal admins
        Map<UUID, AdminUser> userIdMap = new HashMap<>();
        for (AdminUser user : adminUsers) {
            userIdMap.put(user.getId(), user);
        }
        for (PortalAdminUser portalAdminUser : portalAdminUsers) {
            userIdMap.get(portalAdminUser.getAdminUserId())
                .getPortalAdminUsers().add(portalAdminUser);
        }
        return adminUsers;
    }
}
