package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class ResponseSnapshot extends BaseEntity {
    private UUID adminUserId;
    private UUID participantUserId;
    private UUID surveyResponseId;
    // the JSON that surveyJS needs to pick up a survey where it was last left, stored as string for performance reasons
    private String resumeData;
    // ResponseFullData, stored as a string for performance and simplicity
    private String fullData;
}
