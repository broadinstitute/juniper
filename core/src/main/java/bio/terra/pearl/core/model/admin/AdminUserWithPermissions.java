package bio.terra.pearl.core.model.admin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminUserWithPermissions (AdminUser user, Map<UUID, List<String>> portalPermissions) { }

