package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.Permission;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.PermissionService;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class PermissionPopulator extends BasePopulator<Permission, Permission, FilePopulateContext> {
    private PermissionService permissionService;

    public PermissionPopulator(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    protected Class<Permission> getDtoClazz() {
        return Permission.class;
    }

    @Override
    public Permission createNew(Permission popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        return permissionService.create(popDto);
    }

    @Override
    public Permission createPreserveExisting(Permission existingObj, Permission popDto, FilePopulateContext context) throws IOException {
        throw new RuntimeException("Permissions are immutable");
    }

    @Override
    public Permission overwriteExisting(Permission existingObj, Permission popDto, FilePopulateContext context) throws IOException {
        permissionService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Optional<Permission> findFromDto(Permission popDto, FilePopulateContext context) {
        return permissionService.findByName(popDto.getName());
    }
}
