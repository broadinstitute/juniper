package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * corresponds roughly to a form submission.
 * So, in the simple case, a SurveyResponse will have one ResponseSnapshot that contains the data that the user submitted
 * If the user goes back later and edits and resubmits the survey,
 * that updated data will be in a second snapshot attached to the same response
 * The main goal is that anything that a user submits is immutable, so we can trace survey data over time
 */
@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class ResponseSnapshot extends BaseEntity {
    // either the adminUserId or participantUserId represents the user who submitted this data.  Only one should
    // exist for a given response
    private UUID creatingAdminUserId;
    private UUID creatingParticipantUserId;
    private UUID surveyResponseId;
    // the JSON that surveyJS needs to pick up a survey where it was last left, stored as string for performance reasons
    private String resumeData;
    // ResponseData, stored as a string for performance and simplicity--only captured once the snapshot is submitted
    private String fullData;
}
