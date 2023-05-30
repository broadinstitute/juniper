package bio.terra.pearl.populate.dto.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class KitRequestDto extends KitRequest {
    private String creatingAdminUsername;
    private String kitTypeName;
    private String statusName;
}
