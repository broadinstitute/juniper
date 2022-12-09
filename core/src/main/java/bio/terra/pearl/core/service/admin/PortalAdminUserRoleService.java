package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.PortalAdminUserRoleDao;
import bio.terra.pearl.core.model.admin.PortalAdminUserRole;
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

    // TODO: Should this take a PortalAdminUser ID or an AdminUserId and a Portal ID (or shortcode)?
    @Transactional
    public List<PortalAdminUserRole> setRoles(UUID portalAdminUserId, List<String> roleNames) throws ValidationException {
        var portalAdminUser = portalAdminUserService.findByPortalAdminUserId(portalAdminUserId).orElseThrow(() -> new UserNotFoundException(portalAdminUserId));
        var roles = roleNames.stream().map(roleName -> roleService.findByName(roleName).orElseThrow(() -> new RoleNotFoundException(roleName))).toList();

        portalAdminUserRoleDao.deleteByPortalAdminUserId(portalAdminUser.getId());
        var adminUserRoles = roles.stream().map(role -> {
            PortalAdminUserRole portalAdminUserRole = PortalAdminUserRole.builder().portalAdminUserId(portalAdminUserId).roleId(role.getId()).build();
            return portalAdminUserRoleDao.create(portalAdminUserRole);
        }).toList();

        return adminUserRoles;
    }
}
