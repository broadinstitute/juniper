package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.PermissionService;
import bio.terra.pearl.populate.dto.PermissionDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionPopulator extends BasePopulator<Permission, PermissionDto, FilePopulateContext>{
    private final PermissionService permissionService;

    public PermissionPopulator(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected Class<PermissionDto> getDtoClazz() {
        return PermissionDto.class;
    }

    @Override
    public Permission createNew(PermissionDto popDto, FilePopulateContext context, boolean overwrite) {
        return permissionService.create(popDto);
    }

    @Override
    public Permission overwriteExisting(Permission existingObj, PermissionDto popDto, FilePopulateContext context) {
        permissionService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Permission createPreserveExisting(Permission existingObj, PermissionDto popDto, FilePopulateContext context) {
        popDto.setId(existingObj.getId());
        return permissionService.update(popDto);
    }

    @Override
    public Optional<Permission> findFromDto(PermissionDto popDto, FilePopulateContext context) {
        return permissionService.findByName(popDto.getName());
    }

}
