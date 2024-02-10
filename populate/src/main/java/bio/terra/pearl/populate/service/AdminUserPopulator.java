package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.admin.RoleService;
import bio.terra.pearl.populate.dto.AdminUserPopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AdminUserPopulator extends BasePopulator<AdminUser, AdminUserPopDto, FilePopulateContext> {
    private AdminUserService adminUserService;
    private PortalAdminUserService portalAdminUserService;
    private RoleService roleService;
    private PortalAdminUserRoleService portalAdminUserRoleService;

    public AdminUserPopulator(AdminUserService adminUserService, PortalAdminUserService portalAdminUserService, RoleService roleService, PortalAdminUserRoleService portalAdminUserRoleService) {
        this.adminUserService = adminUserService;
        this.portalAdminUserService = portalAdminUserService;
        this.roleService = roleService;
        this.portalAdminUserRoleService = portalAdminUserRoleService;
    }

    public AdminUser populateForPortal(AdminUserPopDto popDto, PortalPopulateContext context,
                                       boolean overwrite, Portal portal) throws IOException {
        AdminUser user = populateFromDto(popDto, context, overwrite);
        PortalAdminUser portalAdminUser = portalAdminUserService.create(PortalAdminUser.builder()
                .adminUserId(user.getId())
                .portalId(portal.getId())
                .build());
        portalAdminUserRoleService.setRoles(portalAdminUser.getId(), popDto.getRoleNames());
        return user;
    }

    @Override
    protected Class<AdminUserPopDto> getDtoClazz() {
        return AdminUserPopDto.class;
    }

    @Override
    public AdminUser createNew(AdminUserPopDto popDto, FilePopulateContext context, boolean overwrite) {
        return adminUserService.create(popDto);
    }

    @Override
    public AdminUser createPreserveExisting(AdminUser existingObj, AdminUserPopDto popDto, FilePopulateContext context) {
        popDto.setId(existingObj.getId());
        return adminUserService.update(popDto);
    }

    @Override
    public AdminUser overwriteExisting(AdminUser existingObj, AdminUserPopDto popDto, FilePopulateContext context) {
        adminUserService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Optional<AdminUser> findFromDto(AdminUserPopDto popDto, FilePopulateContext context ) {
        return adminUserService.findByUsername(popDto.getUsername());
    }
}
