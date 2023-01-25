package bio.terra.pearl.core.model.admin;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Role extends BaseEntity {
    private String name;
    private String displayName;
    private String description;
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
