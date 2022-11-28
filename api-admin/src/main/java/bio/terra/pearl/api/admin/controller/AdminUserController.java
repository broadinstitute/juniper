package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.AdminUserApi;
import bio.terra.pearl.api.admin.model.AdminUserDto;
import bio.terra.pearl.core.service.AdminUserService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AdminUserController implements AdminUserApi {

  private AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @Override
  public ResponseEntity<AdminUserDto> get(UUID id) {
    Optional<AdminUserDto> adminUserDtoOpt =
        adminUserService
            .getAdminUser(id)
            .map(
                adminUser -> {
                  AdminUserDto adminUserDto = new AdminUserDto();
                  BeanUtils.copyProperties(adminUser, adminUserDto);
                  return adminUserDto;
                });
    return ResponseEntity.of(adminUserDtoOpt);
  }
}
