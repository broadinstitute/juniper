package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.AdminUserDao;
import bio.terra.pearl.core.model.admin.*;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.CascadeProperty;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService extends AdminDataAuditedService<AdminUser, AdminUserDao> {
    private final PortalAdminUserService portalAdminUserService;
    private final PortalAdminUserRoleService portalAdminUserRoleService;
    private final AdminDataChangeService adminDataChangeService;
    private final ObjectMapper objectMapper;

    public AdminUserService(AdminUserDao adminUserDao,
                            PortalAdminUserService portalAdminUserService,
                            PortalAdminUserRoleService portalAdminUserRoleService,
                            AdminDataChangeService adminDataChangeService,
                            ObjectMapper objectMapper) {
        super(adminUserDao, adminDataChangeService, objectMapper);
        this.portalAdminUserService = portalAdminUserService;
        this.portalAdminUserRoleService = portalAdminUserRoleService;
        this.adminDataChangeService = adminDataChangeService;
        this.objectMapper = objectMapper;
    }

    public Optional<AdminUser> findByUsername(String username) {
        return dao.findByUsername(username);
    }

    /** Optimized load of just permissions -- no roles */
    public Optional<AdminUserWithPermissions> findByUsernameWithPermissions(String username) {
        return dao.findByUsernameWithPermissions(username);
    }

    @Transactional
    public AdminUser create(AdminUser adminUser, DataAuditInfo auditInfo) {
        //An AdminUser could belong to more than one portal, so we need to check if the user already exists
        //before creating. If the user exists, we'll just add the new portal to the existing user.
        AdminUser savedUser = dao.findByUsername(adminUser.getUsername())
                                 .orElseGet(() -> super.create(adminUser, auditInfo));

        logger.info("Created AdminUser - id: {}, username: {}", savedUser.getId(), savedUser.getUsername());
        for (PortalAdminUser portalAdminUser : adminUser.getPortalAdminUsers()) {
            portalAdminUser.setAdminUserId(savedUser.getId());
            savedUser.getPortalAdminUsers().add(portalAdminUserService.create(portalAdminUser, auditInfo));
        }
        return savedUser;
    }

    @Transactional
    @Override
    public void delete(UUID adminUserId, DataAuditInfo auditInfo, Set<CascadeProperty> cascade) {
        portalAdminUserService.deleteByUserId(adminUserId, auditInfo);
        adminDataChangeService.deleteByResponsibleAdminUserId(adminUserId);
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
                .getPortalAdminUserRoles().add(portalAdminUserRole);
        }
    }

    /** note this should be called AFTER the model has been saved, so the generated ID can be included,
     * but remember to include attached portalParticipantUsers attached */
    protected AdminDataChange makeCreationChangeRecord(AdminUser newModel, DataAuditInfo auditInfo) {
        try {
            AdminDataChange adminDataChange  = AdminDataChange.fromAuditInfo(auditInfo)
                    .modelName(newModel.getClass().getSimpleName())
                    .modelId(newModel.getId())
                    .newValue(objectMapper.writeValueAsString(newModel))
                    .oldValue(null)
                    .build();
            return adminDataChange;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize for audit log", e);
        }
    }
}
