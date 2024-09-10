package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Role extends BaseEntity {
    private String name;
    private String displayName;
    private String description;
    @Builder.Default
    private List<RolePermission> rolePermissions = new ArrayList<>();

    // convenience DTO method
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();
}
