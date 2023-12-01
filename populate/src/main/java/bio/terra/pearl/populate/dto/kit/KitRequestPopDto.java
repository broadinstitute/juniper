package bio.terra.pearl.populate.dto.kit;

import bio.terra.pearl.core.model.kit.KitRequest;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class KitRequestPopDto extends KitRequest {
    private String creatingAdminUsername;
    private String kitTypeName;
    private JsonNode externalRequestJson;
}
