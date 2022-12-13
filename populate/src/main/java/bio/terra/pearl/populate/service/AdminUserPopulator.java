package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.AdminUser;
import bio.terra.pearl.core.service.AdminUserService;
import bio.terra.pearl.populate.dto.AdminUserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AdminUserPopulator extends Populator<AdminUser> {
    private AdminUserService adminUserService;

    public AdminUserPopulator(ObjectMapper objectMapper, FilePopulateService filePopulateService,
                              AdminUserService adminUserService) {
        this.objectMapper = objectMapper;
        this.filePopulateService = filePopulateService;
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
