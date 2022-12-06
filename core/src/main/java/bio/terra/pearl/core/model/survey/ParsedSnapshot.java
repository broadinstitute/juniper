package bio.terra.pearl.core.model.survey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Object model for survey response data.
 * Note that we serialize the data as strings for simplicity and performance, but this is the underlying model
 */
@Getter @Setter @NoArgsConstructor
@SuperBuilder
public class ParsedSnapshot {
    private UUID adminUserId;
    private UUID participantUserId;
    private UUID surveyResponseId;
    private ResponseData data;

    @Getter @Setter @NoArgsConstructor
    public static class ResponseData {
        private List<ResponseDataItem> items;
    }
}
