package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserRoleDao;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import bio.terra.pearl.core.service.exception.ValidationException;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAdminUserRoleService extends ImmutableEntityService<PortalAdminUserRole, PortalAdminUserRoleDao> {

    private PermissionService permissionService;

    private PortalAdminUserService portalAdminUserService;

    private RolePermissionService rolePermissionService;

    private RoleService roleService;

    public PortalAdminUserRoleService(PermissionService permissionService,
                                      PortalAdminUserRoleDao portalAdminUserRoleDao,
                                      @Lazy PortalAdminUserService portalAdminUserService,
                                      RolePermissionService rolePermissionService,
                                      RoleService roleService) {
        super(portalAdminUserRoleDao);
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
    public List<String> setRoles(UUID portalAdminUserId, List<String> roleNames) throws ValidationException {
        var portalAdminUser = portalAdminUserService.find(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        var roles = roleNames.stream().map(roleName -> roleService.findByName(roleName).orElseThrow(() -> new RoleNotFoundException(roleName))).toList();

        dao.deleteByPortalAdminUserId(portalAdminUser.getId());
        roles.forEach(role -> {
            PortalAdminUserRole portalAdminUserRole = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUserId).roleId(role.getId()).build();
            dao.create(portalAdminUserRole);
        });

        return roles.stream().map(Role::getName).toList();
    }

    public List<PortalAdminUserRole> findByPortalAdminUserIdWithRolesAndPermissions(UUID portalAdminUserId) {
        var portalAdminUserRoles = dao.findByPortalAdminUserId(portalAdminUserId);
        portalAdminUserRoles.forEach(portalAdminUserRole -> {
            roleService.findOne(portalAdminUserRole.getRoleId())
                    .ifPresent(role -> {
                        var rolePermissions = rolePermissionService.findByRole(role.getId());
                        rolePermissions.forEach(rolePermission -> {
                            permissionService.find(rolePermission.getPermissionId()).ifPresent(permission -> {
                                role.getPermissions().add(permission);
                            });
                        });
                        portalAdminUserRole.setRole(role);
                    });
        });
        return portalAdminUserRoles;
    }

    public void deleteByPortalAdminUserId(UUID portalAdminUserId) {
        dao.deleteByPortalAdminUserId(portalAdminUserId);
    }
}
