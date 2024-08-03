package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.admin.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class RoleDto extends Role {
    @Builder.Default
    private List<String> permissionNames = new ArrayList<>();
}
