package bio.terra.pearl.api.admin.service.admin;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.notification.email.AdminEmailService;
import bio.terra.pearl.core.service.notification.email.EmailTemplateService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminUserExtService {
  private AdminUserService adminUserService;
  private AuthUtilService authUtilService;
  private AdminEmailService adminEmailService;
  private PortalAdminUserService portalAdminUserService;

  public AdminUserExtService(
      AdminUserService adminUserService,
      AuthUtilService authUtilService,
      AdminEmailService adminEmailService,
      EmailTemplateService emailTemplateService,
      PortalAdminUserService portalAdminUserService) {
    this.adminUserService = adminUserService;
    this.authUtilService = authUtilService;
    this.adminEmailService = adminEmailService;
    this.portalAdminUserService = portalAdminUserService;
  }

  /**
   * gets an adminUser with associated portal admin users, roles, and permissions. If portalId is
   * null, returns all portalAdminUsers, otherwise, just returns the portalAdminUser corresponding
   * to the portalId if one exists
   */
  public AdminUser get(UUID id, String portalShortcode, AdminUser operator) {
    if (portalShortcode == null && !operator.isSuperuser()) {
      // only superusers can see all PortalAdminUsers for an AdminUser
      throw new PermissionDeniedException("You do not have permission for this operation");
    }
    Portal portal = null;
    if (portalShortcode != null) {
      portal = authUtilService.authUserToPortal(operator, portalShortcode);
    }

    Optional<AdminUser> adminUserOpt = adminUserService.find(id);
    if (adminUserOpt.isEmpty()) {
      throw new NotFoundException("No admin user with id %s".formatted(id.toString()));
    }
    AdminUser adminUser = adminUserOpt.get();

    List<PortalAdminUser> portalAdminUsers =
        portalAdminUserService.findByAdminUser(adminUser.getId());
    for (PortalAdminUser portalAdminUser : portalAdminUsers) {
      if (portal == null || portalAdminUser.getPortalId().equals(portal.getId())) {
        portalAdminUserService.attachRolesAndPermissions(portalAdminUser);
        adminUser.getPortalAdminUsers().add(portalAdminUser);
      }
    }
    if (adminUser.getPortalAdminUsers().size() == 0 && !operator.isSuperuser()) {
      // if the user isn't in the requested portal, this user doesn't have access--throw as not
      // found for privacy
      throw new NotFoundException("No admin user with id %s".formatted(id.toString()));
    }
    return adminUser;
  }

  public List<AdminUser> getAll(AdminUser operator) {
    if (operator.isSuperuser()) {
      return adminUserService.findAllWithRoles();
    }
    throw new PermissionDeniedException(
        "User %s does not have permissions to list all users".formatted(operator.getUsername()));
  }

  public List<AdminUser> findByPortal(String portalShortcode, AdminUser operator) {
    Portal portal = authUtilService.authUserToPortal(operator, portalShortcode);
    return adminUserService.findAllWithRolesByPortal(portal.getId());
  }

  public AdminUser create(NewAdminUser newUserParams, AdminUser operator) {
    if (newUserParams.superuser && !operator.isSuperuser()) {
      throw new PermissionDeniedException(
          "User %s does not have permissions to create superusers"
              .formatted(operator.getUsername()));
    }
    AdminUser newUser =
        AdminUser.builder()
            .username(newUserParams.username)
            .superuser(newUserParams.superuser)
            .build();
    if (newUserParams.portalShortcode != null) {
      Portal portal = authUtilService.authUserToPortal(operator, newUserParams.portalShortcode);
      PortalAdminUser paUser = PortalAdminUser.builder().portalId(portal.getId()).build();
      newUser.getPortalAdminUsers().add(paUser);
    }
    AdminUser newAdminUser = adminUserService.create(newUser);
    adminEmailService.sendWelcomeEmail(null, newAdminUser);
    return newAdminUser;
  }

  public record NewAdminUser(String username, boolean superuser, String portalShortcode) {}
}
