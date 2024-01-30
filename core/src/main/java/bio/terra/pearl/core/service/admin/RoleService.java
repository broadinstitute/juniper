package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.RoleDao;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.model.admin.Role;

import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import bio.terra.pearl.core.model.admin.RolePermission;
import java.util.List;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService extends ImmutableEntityService<Role, RoleDao> {
    private PermissionService permissionService;
    private RolePermissionService rolePermissionService;

    public RoleService(RoleDao roleDao, PermissionService permissionService, RolePermissionService rolePermissionService) {
        super(roleDao);
        this.permissionService = permissionService;
        this.rolePermissionService = rolePermissionService;
    }

    @Transactional
    public Role create(Role role) {
        return dao.create(role);
    }

    /** Creates the role and attaches the given permissions. If any of the names don't exist, an error will be thrown */
    @Transactional
    public Role create(Role role, List<String> permissionNames) {
        Role savedRole = dao.create(role);
        for (String permissionName : permissionNames) {
            Permission perm = permissionService.findByName(permissionName).get();
            RolePermission rolePerm = rolePermissionService.create(RolePermission.builder()
                    .roleId(savedRole.getId())
                    .permissionId(perm.getId())
                    .build());
            savedRole.getPermissions().add(perm);
        }
        return savedRole;
    }

    public Optional<Role> findOne(UUID roleId) { return dao.find(roleId); }

    public Optional<Role> findByName(String roleName) {
        return dao.findByName(roleName);
    }
}
