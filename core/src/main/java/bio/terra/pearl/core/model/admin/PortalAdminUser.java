package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class PortalAdminUser extends BaseEntity {
    private UUID adminUserId;
    private UUID portalId;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
