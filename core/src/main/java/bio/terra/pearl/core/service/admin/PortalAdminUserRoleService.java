package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserRoleDao;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.exception.RoleNotFoundException;
import bio.terra.pearl.core.service.exception.UserNotFoundException;
import bio.terra.pearl.core.service.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PortalAdminUserRoleService {

    private PortalAdminUserRoleDao portalAdminUserRoleDao;

    private PortalAdminUserService portalAdminUserService;

    private RoleService roleService;

    public PortalAdminUserRoleService(PortalAdminUserRoleDao portalAdminUserRoleDao, PortalAdminUserService portalAdminUserService, RoleService roleService) {
        this.portalAdminUserRoleDao = portalAdminUserRoleDao;
        this.portalAdminUserService = portalAdminUserService;
        this.roleService = roleService;
    }

    public List<PortalAdminUserRole> getRolesForAdminUser(UUID adminUserId) {
        return portalAdminUserRoleDao.findByPortalAdminUserId(adminUserId);
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
        var portalAdminUser = portalAdminUserService.findOne(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        var roles = roleNames.stream().map(roleName -> roleService.findByName(roleName).orElseThrow(() -> new RoleNotFoundException(roleName))).toList();

        portalAdminUserRoleDao.deleteByPortalAdminUserId(portalAdminUser.getId());
        roles.forEach(role -> {
            PortalAdminUserRole portalAdminUserRole = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUserId).roleId(role.getId()).build();
            portalAdminUserRoleDao.create(portalAdminUserRole);
        });

        return roles.stream().map(Role::getName).toList();
    }
}
