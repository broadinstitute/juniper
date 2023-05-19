package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.admin.RolePermission;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.PermissionService;
import bio.terra.pearl.core.service.admin.RolePermissionService;
import bio.terra.pearl.core.service.admin.RoleService;
import bio.terra.pearl.populate.dto.admin.RolePopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class RolePopulator extends BasePopulator<Role, RolePopDto, FilePopulateContext> {
    private PermissionService permissionService;
    private RolePermissionService rolePermissionService;
    private RoleService roleService;

    public RolePopulator(PermissionService permissionService,
                         RolePermissionService rolePermissionService,
                         RoleService roleService) {
        this.permissionService = permissionService;
        this.rolePermissionService = rolePermissionService;
        this.roleService = roleService;
    }

    @Override
    protected Class<RolePopDto> getDtoClazz() {
        return RolePopDto.class;
    }

    @Override
    public Role createNew(RolePopDto popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        var role = roleService.create(popDto);
        for (String permissionName : popDto.getPermissionNames()) {
            var permission = permissionService.findByName(permissionName);
            RolePermission rolePermission = RolePermission.builder()
                    .roleId(role.getId())
                    .permissionId(permission.get().getId())
                    .build();
            rolePermissionService.create(rolePermission);
        }
        return role;
    }

    @Override
    public Role createPreserveExisting(Role existingObj, RolePopDto popDto, FilePopulateContext context) throws IOException {
        throw new RuntimeException("Roles are immutable");
    }

    @Override
    public Role overwriteExisting(Role existingObj, RolePopDto popDto, FilePopulateContext context) throws IOException {
        roleService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Optional<Role> findFromDto(RolePopDto popDto, FilePopulateContext context) {
        return roleService.findByName(popDto.getName());
    }
}
