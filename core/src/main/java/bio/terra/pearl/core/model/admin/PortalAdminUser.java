package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class PortalAdminUser extends BaseEntity {
    private UUID adminUserId;
    private UUID portalId;
    @Builder.Default
    private List<PortalAdminUserRole> portalAdminUserRoles = new ArrayList<>();

    // convenience DTO holder for all roles of the PortalAdminUser
    @Builder.Default
    private List<Role> roles = new ArrayList<>();
}
