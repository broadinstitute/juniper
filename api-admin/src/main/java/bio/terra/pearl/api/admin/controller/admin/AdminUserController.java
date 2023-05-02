package bio.terra.pearl.api.admin.controller.admin;

import bio.terra.pearl.api.admin.api.AdminUserApi;
import bio.terra.pearl.api.admin.model.AdminUserDto;
import bio.terra.pearl.api.admin.model.ErrorReport;
import bio.terra.pearl.api.admin.model.RoleList;
import bio.terra.pearl.api.admin.service.AdminUserExtService;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminUserController implements AdminUserApi {
  private AdminUserExtService adminUserExtService;
  private AuthUtilService authUtilService;
  private PortalAdminUserRoleService portalAdminUserRoleService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public AdminUserController(
      AdminUserExtService adminUserExtService,
      AuthUtilService authUtilService,
      PortalAdminUserRoleService portalAdminUserRoleService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.adminUserExtService = adminUserExtService;
    this.authUtilService = authUtilService;
    this.portalAdminUserRoleService = portalAdminUserRoleService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorReport handleValidationException(ValidationException e) {
    return new ErrorReport().message(e.getMessage());
  }

  @Override
  public ResponseEntity<AdminUserDto> get(UUID id) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    Optional<AdminUserDto> adminUserDtoOpt =
        adminUserExtService
            .get(id, operator)
            .map(
                adminUser -> {
                  AdminUserDto adminUserDto = new AdminUserDto();
                  BeanUtils.copyProperties(adminUser, adminUserDto);
                  return adminUserDto;
                });
    return ResponseEntity.of(adminUserDtoOpt);
  }

  @Override
  public ResponseEntity<Object> getAll() {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<AdminUser> adminUsers = adminUserExtService.getAll(operator);
    for (AdminUser user : adminUsers) {
      user.setToken(null);
    }
    return ResponseEntity.ok(adminUsers);
  }

  @Override
  public ResponseEntity<Object> getByPortal(String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<AdminUser> adminUsers = adminUserExtService.getByPortal(portalShortcode, operator);
    for (AdminUser user : adminUsers) {
      user.setToken(null);
    }
    return ResponseEntity.ok(adminUsers);
  }

  @Override
  public ResponseEntity<Object> create(Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    AdminUserExtService.NewAdminUser newUser =
        objectMapper.convertValue(body, AdminUserExtService.NewAdminUser.class);
    if (!newUser.superuser() && newUser.portalShortcode() == null) {
      throw new ValidationException(
          "Created user must be either a superuser or associated with at least one portal");
    }
    AdminUser createdUser = adminUserExtService.create(newUser, operator);
    return ResponseEntity.ok(createdUser);
  }

  // TODO: return something useful here... but what? PortalAdminUserRoles? Role names?
  @Override
  public ResponseEntity<RoleList> setRoles(UUID userId, RoleList body) throws ValidationException {
    var roleNames = portalAdminUserRoleService.setRoles(userId, body.getRoles());
    return ResponseEntity.ok(new RoleList().roles(roleNames));
  }
}
