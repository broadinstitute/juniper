package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * PreEnrollment response is different than a SurveyResponse since it may come while a user is not authenticated,
 * and therefore never should be resumed, and so doesn't need snapshots -- it can only be created once and then
 * the data is never updated.
 * However, if the user goes on to enroll, this response should be linked to the enrollee, so that the data
 * can be referenced elsewhere.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PreEnrollmentResponse extends BaseEntity {
    private UUID portalParticipantUserId;
    /** if the user was already signed into another portal, we might be able to capture that user id here */
    private UUID creatingParticipantUserId;
    private UUID surveyId;
    private UUID studyEnvironmentId;
    private UUID enrolleeId;
    private String fullData;
    @Builder.Default
    private boolean qualified = false; // whether or not the responses meet the criteria for eligibility.
}
