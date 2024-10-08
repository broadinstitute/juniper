package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.RoleDao;
import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.model.admin.Role;

import bio.terra.pearl.core.service.CrudService;
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
public class RoleService extends CrudService<Role, RoleDao> {
    private final PermissionService permissionService;
    private final RolePermissionService rolePermissionService;

    public RoleService(RoleDao roleDao, PermissionService permissionService, RolePermissionService rolePermissionService) {
        super(roleDao);
        this.permissionService = permissionService;
        this.rolePermissionService = rolePermissionService;
    }

    @Transactional
    public Role create(Role role) {
        Role createdRole = dao.create(role);
        role.getPermissions().forEach(permission -> {
            rolePermissionService.create(RolePermission.builder()
                    .roleId(createdRole.getId())
                    .permissionId(permission.getId())
                    .build());
        });

        return createdRole;
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

    @Transactional
    public Role update(Role role) {
        rolePermissionService.deleteByRoleId(role.getId());

        role.getPermissions().forEach(permission -> {
            rolePermissionService.create(RolePermission.builder()
                    .roleId(role.getId())
                    .permissionId(permission.getId())
                    .build());
        });

        return dao.update(role);
    }

    public void attachPermissions(List<Role> roles) {
        List<RolePermission> rolePermissions = rolePermissionService.findAllByRoleIds(roles.stream().map(Role::getId).toList());
        List<Permission> permissions = permissionService.findAll(rolePermissions.stream().map(RolePermission::getPermissionId).toList());
        roles.forEach(role -> {
            List<Permission> rolePermissionsList = rolePermissions.stream()
                    .filter(rolePermission -> rolePermission.getRoleId().equals(role.getId()))
                    .map(rolePermission -> permissions.stream()
                            .filter(permission -> permission.getId().equals(rolePermission.getPermissionId()))
                            .findFirst()
                            .orElseThrow())
                    .toList();
            role.setPermissions(rolePermissionsList);
        });
    }

    public Optional<Role> findByName(String roleName) {
        return dao.findByName(roleName);
    }
}
