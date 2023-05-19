package bio.terra.pearl.populate.dto.admin;

import bio.terra.pearl.core.model.admin.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class RolePopDto extends Role {
    private List<String> permissionNames = new ArrayList<>();
}
