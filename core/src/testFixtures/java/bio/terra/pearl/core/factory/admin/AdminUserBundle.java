package bio.terra.pearl.core.factory.admin;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.admin.PortalAdminUser;

import java.util.List;

public record AdminUserBundle(AdminUser user, List<PortalAdminUser> portalAdminUsers) {
}
