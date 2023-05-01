package bio.terra.pearl.api.admin.controller.admin;

import bio.terra.pearl.api.admin.service.AdminUserExtService;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import javax.servlet.http.HttpServletRequest;

public class PortalAdminUserController {
  private AuthUtilService authUtilService;
  private AdminUserExtService adminUserExtService;
  private HttpServletRequest request;

  public PortalAdminUserController(
      AuthUtilService authUtilService,
      AdminUserExtService adminUserExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.adminUserExtService = adminUserExtService;
    this.request = request;
  }
}
