package bio.terra.pearl.api.admin;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;

public class MockAuthServiceAlwaysRejects extends AuthUtilService {
  public MockAuthServiceAlwaysRejects() {
    super(null, null, null, null, null, null, null);
  }

  @Override
  public Portal authUserToPortal(AdminUser user, String portalShortcode) {
    throw new PermissionDeniedException(
        "User %s does not have permissions on portal %s"
            .formatted(user.getUsername(), portalShortcode));
  }
}
