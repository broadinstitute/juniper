package bio.terra.pearl.populate.dto.consent;

import bio.terra.pearl.core.model.consent.ConsentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ConsentResponsePopDto extends ConsentResponse {
    private String consentStableId;
    private int consentVersion;
    private JsonNode fullDataJson;
    private JsonNode resumeDataJson;
}
