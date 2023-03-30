package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.populate.dto.AdminUserDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AdminUserPopulator extends Populator<AdminUser, FilePopulateContext> {
    private AdminUserService adminUserService;
    private PortalAdminUserService portalAdminUserService;

    public AdminUserPopulator(AdminUserService adminUserService, PortalAdminUserService portalAdminUserService) {
        this.adminUserService = adminUserService;
        this.portalAdminUserService = portalAdminUserService;
    }

    @Override
    public AdminUser populateFromString(String content, FilePopulateContext context) throws JsonProcessingException {
        AdminUserDto adminUserDto = objectMapper.readValue(content, AdminUserDto.class);
        Optional<AdminUser> existingUserOpt = adminUserService.findByUsername(adminUserDto.getUsername());
        existingUserOpt.ifPresent(existingUser -> {
            adminUserService.delete(existingUser.getId(), new HashSet<>());
        });
        return adminUserService.create(adminUserDto);
    }

    public AdminUser populateForPortal(AdminUserDto userDto, Portal portal) {
        Optional<AdminUser> userOpt = adminUserService.findByUsername(userDto.getUsername());
        userOpt.ifPresent(existingUser -> {
            adminUserService.delete(existingUser.getId(), CascadeProperty.EMPTY_SET);
        });
        AdminUser user = adminUserService.create(userDto);
        portalAdminUserService.create(PortalAdminUser.builder()
                .adminUserId(user.getId())
                .portalId(portal.getId())
                .build());
        return user;
    }
}
