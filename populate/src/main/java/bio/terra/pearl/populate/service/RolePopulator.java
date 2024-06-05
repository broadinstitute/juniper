package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.RoleService;
import bio.terra.pearl.populate.dto.RoleDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RolePopulator extends BasePopulator<Role, RoleDto, FilePopulateContext>{

    private RoleService roleService;

    public RolePopulator(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    protected Class<RoleDto> getDtoClazz() { return RoleDto.class; }

    @Override
    public Optional<Role> findFromDto(RoleDto popDto, FilePopulateContext context) {
        return roleService.findByName(popDto.getName());
    }

    @Override
    public Role createNew(RoleDto popDto, FilePopulateContext context, boolean overwrite) {
        return roleService.create(popDto);
    }

    @Override
    public Role overwriteExisting(Role existingObj, RoleDto popDto, FilePopulateContext context) {
        roleService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Role createPreserveExisting(Role existingObj, RoleDto popDto, FilePopulateContext context) {
        popDto.setId(existingObj.getId());
        return roleService.update(popDto);
    }
}
