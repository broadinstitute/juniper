package bio.terra.pearl.api.admin.service.admin;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.admin.Role;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.admin.PortalAdminUserRoleService;
import bio.terra.pearl.core.service.admin.PortalAdminUserService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.notification.email.AdminEmailService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserExtService {
  private final AdminUserService adminUserService;
  private final AdminEmailService adminEmailService;
  private final PortalAdminUserService portalAdminUserService;
  private final PortalAdminUserRoleService portalAdminUserRoleService;

  public AdminUserExtService(
      AdminUserService adminUserService,
      AdminEmailService adminEmailService,
      PortalAdminUserService portalAdminUserService,
      PortalAdminUserRoleService portalAdminUserRoleService) {
    this.adminUserService = adminUserService;
    this.adminEmailService = adminEmailService;
    this.portalAdminUserService = portalAdminUserService;
    this.portalAdminUserRoleService = portalAdminUserRoleService;
  }

  @SuperuserOnly
  public AdminUser get(OperatorAuthContext authContext, UUID id) {
    return get(id, null, authContext.getOperator());
  }

  /**
   * gets an adminUser with associated portal admin users, roles, and permissions. If portalId is
   * null, returns all portalAdminUsers, otherwise, just returns the portalAdminUser corresponding
   * to the portalId if one exists
   */
  @EnforcePortalPermission(permission = "BASE")
  public AdminUser getInPortal(PortalAuthContext authContext, UUID id) {
    return get(id, authContext.getPortal(), authContext.getOperator());
  }

  /**
   * if portal is specified, will only return the user if they are in the portal, and will only
   * return PortalAdminUser info related to that portal. if portal is null, will return all
   * information about the user
   */
  private AdminUser get(UUID id, Portal portal, AdminUser operator) {
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

  @SuperuserOnly
  public List<AdminUser> getAll(OperatorAuthContext authContext) {
    return adminUserService.findAllWithRoles();
  }

  @EnforcePortalPermission(permission = "BASE")
  public List<AdminUser> findByPortal(PortalAuthContext authContext) {
    return adminUserService.findAllWithRolesByPortal(authContext.getPortal().getId());
  }

  @SuperuserOnly
  public AdminUser createSuperuser(OperatorAuthContext authContext, String username) {
    AdminUser newUser = AdminUser.builder().username(username).superuser(true).build();
    DataAuditInfo auditInfo =
        DataAuditInfo.builder().responsibleAdminUserId(authContext.getOperator().getId()).build();
    AdminUser adminUser = adminUserService.create(newUser, auditInfo);
    adminEmailService.sendWelcomeEmail(null, adminUser);
    return adminUser;
  }

  @EnforcePortalPermission(permission = "admin_user_edit")
  @Transactional
  public AdminUser createAdminUser(
      PortalAuthContext authContext, String username, List<String> roleNames) {
    AdminUser newUser = AdminUser.builder().username(username).superuser(false).build();
    newUser
        .getPortalAdminUsers()
        .add(PortalAdminUser.builder().portalId(authContext.getPortal().getId()).build());
    DataAuditInfo auditInfo =
        DataAuditInfo.builder().responsibleAdminUserId(authContext.getOperator().getId()).build();
    AdminUser adminUser = adminUserService.create(newUser, auditInfo);
    if (roleNames.size() > 0) {
      PortalAdminUser paUser = adminUser.getPortalAdminUsers().stream().findFirst().orElseThrow();
      portalAdminUserRoleService.setRoles(paUser.getId(), roleNames, auditInfo);
    }
    adminEmailService.sendWelcomeEmail(authContext.getPortal(), adminUser);
    return adminUser;
  }

  @EnforcePortalPermission(permission = "admin_user_edit")
  public List<String> setPortalUserRoles(
      PortalAuthContext authContext, UUID adminUserId, List<String> roleNames) {

    if (!authContext.getOperator().isSuperuser()) {
      // regular users can only assign roles they have themselves
      PortalAdminUser operatorPaUser =
          portalAdminUserService
              .findByUserIdAndPortal(
                  authContext.getOperator().getId(), authContext.getPortal().getId())
              .orElseThrow();
      portalAdminUserService.attachRolesAndPermissions(operatorPaUser);
      List<String> operatorRoleNames =
          operatorPaUser.getRoles().stream().map(Role::getName).toList();
      roleNames.forEach(
          roleName -> {
            if (!operatorRoleNames.contains(roleName)) {
              throw new PermissionDeniedException(
                  "You do not have permission to assign role %s".formatted(roleName));
            }
          });
    }
    // looking up the portalAdminUser of the target by the portal from the authContext
    // confirms they are in a portal the operator has access and permission to edit users
    PortalAdminUser paUser =
        portalAdminUserService
            .findByUserIdAndPortal(adminUserId, authContext.getPortal().getId())
            .orElseThrow(() -> new NotFoundException("No matching user found"));

    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .adminUserId(adminUserId)
            .build();
    return portalAdminUserRoleService.setRoles(paUser.getId(), roleNames, auditInfo);
  }

  @SuperuserOnly
  public void delete(OperatorAuthContext authContext, UUID adminUserId) {
    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .adminUserId(adminUserId)
            .build();
    adminUserService.delete(adminUserId, auditInfo);
  }

  /**
   * removes the user from the specified portal, if the user is in no other portals, also deletes
   * the user
   */
  @EnforcePortalPermission(permission = "admin_user_edit")
  @Transactional
  public void deleteInPortal(PortalAuthContext authContext, UUID adminUserId) {
    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .adminUserId(adminUserId)
            .build();
    // looking up the portalAdminUser of the target by the portal from the authContext
    // confirms they are in a portal the operator has access and permission to edit users
    PortalAdminUser paUser =
        portalAdminUserService
            .findByUserIdAndPortal(adminUserId, authContext.getPortal().getId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "No admin user with id %s in portal %s"
                            .formatted(
                                adminUserId.toString(), authContext.getPortal().getShortcode())));
    portalAdminUserService.delete(paUser.getId(), auditInfo);

    /** if there are no portal users left for the admin, delete the adminUser too */
    List<PortalAdminUser> portalAdminUsers = portalAdminUserService.findByAdminUser(adminUserId);
    if (portalAdminUsers.size() == 0) {
      adminUserService.delete(adminUserId, auditInfo);
    }
  }
}
