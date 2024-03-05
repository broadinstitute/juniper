package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.admin.AdminUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class AdminUserPopDto extends AdminUser {
    private List<String> roleNames = new ArrayList<>();
}
