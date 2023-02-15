package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.populate.dto.AdminUserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AdminUserPopulator extends Populator<AdminUser> {
    private AdminUserService adminUserService;

    public AdminUserPopulator(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Override
    public AdminUser populateFromString(String content, FilePopulateConfig config) throws JsonProcessingException {
        AdminUserDto adminUserDto = objectMapper.readValue(content, AdminUserDto.class);
        Optional<AdminUser> existingUserOpt = adminUserService.findByUsername(adminUserDto.getUsername());
        existingUserOpt.ifPresent(existingUser -> {
            adminUserService.delete(existingUser.getId(), new HashSet<>());
        });
        return adminUserService.create(adminUserDto);
    }
}
