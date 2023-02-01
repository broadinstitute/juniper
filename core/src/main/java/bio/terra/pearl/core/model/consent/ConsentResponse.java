package bio.terra.pearl.core.model.consent;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Container for response to a consent form.  Unlike SurveyResponses, which store data in snapshots, the data are
 * are contained in the response object itself.  This is because consent forms do not need to support recurrence,
 * admin-editing workflows, and other complexities that surveys must support.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ConsentResponse extends BaseEntity {
    private UUID enrolleeId;
    private UUID creatingParticipantUserId;
    private UUID creatingAdminUserId;
    private UUID consentFormId;
    @Builder.Default
    private boolean completed = false;
    @Builder.Default
    private boolean consented = false;
    // the JSON that surveyJS needs to pick up a survey where it was last left, stored as string for performance reasons
    private String resumeData;
    // ResponseFullData, stored as a string for performance and simplicity
    private String fullData;
}
