package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PortalAdminUserService extends ImmutableEntityService<PortalAdminUser, PortalAdminUserDao> {

    private PortalAdminUserRoleService portalAdminUserRoleService;

    public PortalAdminUserService(PortalAdminUserDao portalAdminUserDao,
                                  PortalAdminUserRoleService portalAdminUserRoleService) {
        super(portalAdminUserDao);
        this.portalAdminUserRoleService = portalAdminUserRoleService;
    }

    public Optional<PortalAdminUser> findOneWithRolesAndPermissions(UUID portalAdminUserId) {
        var portalAdminUserOpt = dao.find(portalAdminUserId);
        return portalAdminUserOpt.map(portalAdminUser -> {
            var portalAdminUserRoles = portalAdminUserRoleService.findByPortalAdminUserIdWithRolesAndPermissions(portalAdminUser.getId());
            portalAdminUserRoles.forEach((PortalAdminUserRole portalAdminUserRole) -> {
                portalAdminUser.getRoles().add(portalAdminUserRole.getRole());
            });
            return portalAdminUser;
        });
    }

    public boolean userHasRole(UUID portalAdminUserId, String roleName) {
        var portalAdminUser = findOneWithRolesAndPermissions(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        return portalAdminUser.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean userHasPermission(UUID portalAdminUserId, String permissionName) {
        var portalAdminUser = findOneWithRolesAndPermissions(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        return portalAdminUser.getRoles().stream().anyMatch(role -> {
            return role.getPermissions().stream().anyMatch(permission -> {
                return permission.getName().equals(permissionName);
            });
        });
    }

    public void deleteByUserId(UUID adminUserId) {
        dao.deleteByUserId(adminUserId);
    }
}
