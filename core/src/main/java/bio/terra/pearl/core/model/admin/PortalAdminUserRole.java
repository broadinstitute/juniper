package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
@ToString
public class PortalAdminUserRole extends BaseEntity {
    private PortalAdminUser portalAdminUser;
    private UUID portalAdminUserId;
    private Role role;
    private UUID roleId;
}
