package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.AdminUserApi;
import bio.terra.pearl.api.admin.model.AdminUserDto;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.model.RoleList;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.ValidationException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminUserController implements AdminUserApi {

  private AdminUserService adminUserService;

  private PortalAdminUserRoleService portalAdminUserRoleService;

  public AdminUserController(
      AdminUserService adminUserService, PortalAdminUserRoleService portalAdminUserRoleService) {
    this.adminUserService = adminUserService;
    this.portalAdminUserRoleService = portalAdminUserRoleService;
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorReport handleValidationException(ValidationException e) {
    return new ErrorReport().message(e.getMessage());
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

  // TODO: return something useful here... but what? PortalAdminUserRoles? Role names?
  @Override
  public ResponseEntity<RoleList> setRoles(UUID userId, RoleList body) throws ValidationException {
    var roleNames = portalAdminUserRoleService.setRoles(userId, body.getRoles());
    return ResponseEntity.ok(new RoleList().roles(roleNames));
  }
}
