package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserRoleDao;
import bio.terra.pearl.core.model.admin.*;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import bio.terra.pearl.core.service.exception.ValidationException;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAdminUserRoleService extends AdminDataAuditedService<PortalAdminUserRole, PortalAdminUserRoleDao> {

    private PermissionService permissionService;

    private PortalAdminUserService portalAdminUserService;

    private RolePermissionService rolePermissionService;

    private RoleService roleService;

    public PortalAdminUserRoleService(PermissionService permissionService,
                                      PortalAdminUserRoleDao portalAdminUserRoleDao,
                                      @Lazy PortalAdminUserService portalAdminUserService,
                                      RolePermissionService rolePermissionService,
                                      RoleService roleService,
                                      AdminDataChangeService adminDataChangeService,
                                      ObjectMapper objectMapper) {
        super(portalAdminUserRoleDao, adminDataChangeService, objectMapper);
        this.permissionService = permissionService;
        this.portalAdminUserService = portalAdminUserService;
        this.rolePermissionService = rolePermissionService;
        this.roleService = roleService;
    }

    public List<PortalAdminUserRole> getRolesForAdminUser(UUID adminUserId) {
        return dao.findByPortalAdminUserId(adminUserId);
    }

    public List<PortalAdminUserRole> findAllByPortalAdminUserIds(List<UUID> portalAdminUserIds) {
        return dao.findAllByPortalAdminUserIds(portalAdminUserIds);
    }

    /**
     * Replaces the roles for the given admin user based on the given role names. Any roles that the admin user may have
     * previously had will be removed if they are not included in the list of role names.
     *
     * @param portalAdminUserId UUID of the portal admin user
     * @param roleNames names of roles to set
     * @return a list of role names that were actually set
     * @throws ValidationException when the portal admin user or any of the roles are not found
     */
    @Transactional
    public List<String> setRoles(UUID portalAdminUserId, List<String> roleNames, DataAuditInfo auditInfo) throws ValidationException {
        List<Role> roles = roleNames.stream().map(roleName -> roleService.findByName(roleName).orElseThrow(() -> new RoleNotFoundException(roleName))).toList();

        List<PortalAdminUserRole> existing = dao.findByPortalAdminUserId(portalAdminUserId);
        List<PortalAdminUserRole> removed = existing.stream().filter(portalAdminUserRole -> roles.stream().noneMatch(role -> role.getId().equals(portalAdminUserRole.getRoleId()))).toList();
        removed.forEach(portalAdminUserRole -> {
            delete(portalAdminUserRole.getId(), auditInfo);
        });
        List<Role> addedRoles = roles.stream().filter(role -> existing.stream().noneMatch(portalAdminUserRole -> portalAdminUserRole.getRoleId().equals(role.getId()))).toList();
        List<PortalAdminUserRole> added = addedRoles.stream().map(role -> {
            PortalAdminUserRole portalAdminUserRole = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUserId).roleId(role.getId()).build();
            return create(portalAdminUserRole, auditInfo);
        }).toList();
        return roles.stream().map(Role::getName).toList();
    }

    public List<Role> findRolesWithPermissionsByPortalAdminUserId(UUID portalAdminUserId) {
        List<PortalAdminUserRole> portalAdminUserRoles = dao.findByPortalAdminUserId(portalAdminUserId);
        List<Role> roles = roleService.findAll(portalAdminUserRoles.stream().map(PortalAdminUserRole::getRoleId).toList());
        roleService.attachPermissions(roles);
        return roles;
    }

    /** this operation is not audited as it is assumed this is in the context of portal admin user deletion, which is */
    protected void deleteByPortalAdminUserId(UUID portalAdminUserId) {
        dao.deleteByPortalAdminUserId(portalAdminUserId);
    }
}
