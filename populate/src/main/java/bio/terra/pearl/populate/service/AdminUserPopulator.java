package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.populate.dto.AdminUserDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AdminUserPopulator extends Populator<AdminUser, AdminUserDto, FilePopulateContext> {
    private AdminUserService adminUserService;
    private PortalAdminUserService portalAdminUserService;

    public AdminUserPopulator(AdminUserService adminUserService, PortalAdminUserService portalAdminUserService) {
        this.adminUserService = adminUserService;
        this.portalAdminUserService = portalAdminUserService;
    }

    public AdminUser populateForPortal(AdminUserDto popDto, PortalPopulateContext context,
                                       boolean overwrite, Portal portal) throws IOException {
        AdminUser user = populateFromDto(popDto, context, overwrite);
        portalAdminUserService.create(PortalAdminUser.builder()
                .adminUserId(user.getId())
                .portalId(portal.getId())
                .build());
        return user;
    }

    @Override
    protected Class<AdminUserDto> getDtoClazz() {
        return AdminUserDto.class;
    }

    @Override
    public AdminUser createNew(AdminUserDto popDto, FilePopulateContext context, boolean overwrite) {
        return adminUserService.create(popDto);
    }

    @Override
    public AdminUser createPreserveExisting(AdminUser existingObj, AdminUserDto popDto, FilePopulateContext context) {
        popDto.setId(existingObj.getId());
        return adminUserService.update(popDto);
    }

    @Override
    public AdminUser overwriteExisting(AdminUser existingObj, AdminUserDto popDto, FilePopulateContext context) {
        return null;
    }

    @Override
    public Optional<AdminUser> findFromDto(AdminUserDto popDto, FilePopulateContext context ) {
        return adminUserService.findByUsername(popDto.getUsername());
    }
}
