package bio.terra.pearl.api.admin.controller.admin;

import bio.terra.pearl.api.admin.api.AdminUserApi;
import bio.terra.pearl.api.admin.service.admin.AdminUserExtService;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @Override
  public ResponseEntity<Object> get(UUID id, String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    AdminUser user = null;
    if (portalShortcode == null) {
      user = adminUserExtService.get(OperatorAuthContext.of(operator), id);
    } else {
      user = adminUserExtService.getInPortal(PortalAuthContext.of(operator, portalShortcode), id);
    }
    return ResponseEntity.ok(user);
  }

  @Override
  public ResponseEntity<Object> getAll() {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<AdminUser> adminUsers = adminUserExtService.getAll(OperatorAuthContext.of(operator));
    return ResponseEntity.ok(adminUsers);
  }

  @Override
  public ResponseEntity<Object> getByPortal(String portalShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<AdminUser> adminUsers =
        adminUserExtService.findByPortal(PortalAuthContext.of(operator, portalShortcode));
    return ResponseEntity.ok(adminUsers);
  }

  /** creates a user outside of any particular portal -- i.e. a superuser */
  @Override
  public ResponseEntity<Object> create(Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    NewAdminUser newUser = objectMapper.convertValue(body, NewAdminUser.class);
    AdminUser createdUser =
        adminUserExtService.createSuperuser(
            OperatorAuthContext.of(operator), newUser.getUsername());
    return new ResponseEntity(createdUser, HttpStatus.CREATED);
  }

  /**
   * creates a user and associates them with a portal, or associates an existing user with a portal
   */
  @Override
  public ResponseEntity<Object> createInPortal(String portalShortcode, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    NewAdminUser newUser = objectMapper.convertValue(body, NewAdminUser.class);
    AdminUser createdUser =
        adminUserExtService.createAdminUser(
            PortalAuthContext.of(operator, portalShortcode),
            newUser.getUsername(),
            newUser.getRoleNames());
    return new ResponseEntity(createdUser, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Object> setRolesInPortal(
      String portalShortcode, UUID adminUserId, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    List<String> roleNames = objectMapper.convertValue(body, new TypeReference<List<String>>() {});
    List<String> updatedRoles =
        adminUserExtService.setPortalUserRoles(
            PortalAuthContext.of(operator, portalShortcode), adminUserId, roleNames);
    return ResponseEntity.ok(updatedRoles);
  }

  /** deletes a user and all associated portal users */
  @Override
  public ResponseEntity<Void> delete(UUID id) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    adminUserExtService.delete(OperatorAuthContext.of(operator), id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteInPortal(String portalShortcode, UUID adminUserId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    adminUserExtService.deleteInPortal(
        PortalAuthContext.of(operator, portalShortcode), adminUserId);
    return ResponseEntity.noContent().build();
  }

  @Getter
  @Setter
  @Builder
  public static class NewAdminUser {
    String username;
    @Builder.Default List<String> roleNames = new ArrayList<>();
  }
}
