package bio.terra.pearl.populate.dto.survey;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ResponseSnapshotPopDto {
    private JsonNode fullDataJson;
    private JsonNode resumeDataJson;
}
