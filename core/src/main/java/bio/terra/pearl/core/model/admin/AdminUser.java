package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class AdminUser extends BaseEntity {
    private String username;
    private Instant lastLogin;
    @Builder.Default
    private boolean superuser = false;
    @Builder.Default
    private List<PortalAdminUser> portalAdminUsers = new ArrayList<>();
}
