package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.AdminUser;
import bio.terra.pearl.core.service.AdminUserService;
import bio.terra.pearl.populate.dto.AdminUserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class AdminUserPopulator extends Populator<AdminUser> {

    private ObjectMapper objectMapper;

    private FilePopulateService filePopulateService;

    private AdminUserService adminUserService;

    public AdminUserPopulator(ObjectMapper objectMapper, FilePopulateService filePopulateService, AdminUserService adminUserService) {
        this.objectMapper = objectMapper;
        this.filePopulateService = filePopulateService;
        this.adminUserService = adminUserService;
    }

    @Transactional
    @Override
    public AdminUser populate(String fileName, FilePopulateConfig config) throws IOException {
        String content = filePopulateService.readFile(fileName, config);
        return populateFromString(content, config);
    }

    public AdminUser populateFromString(String content, FilePopulateConfig config) throws JsonProcessingException {
        AdminUserDto adminUserDto = objectMapper.readValue(content, AdminUserDto.class);
        return adminUserService.createAdminUser(adminUserDto);
    }
}
