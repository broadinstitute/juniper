package bio.terra.pearl.populate.dto.survey;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ResponseSnapshotPopDto {
    private JsonNode fullDataJson;
    private JsonNode resumeDataJson;
    /** if true, snapshotProcessingService.processAllAnswerMappings will be invoked after the response is populated.
     * set to false when you want fine-grained control over the DataRecordChange history for a participant  */
    private boolean processSnapshot = true;
}
