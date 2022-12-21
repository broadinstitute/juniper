package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Preregistration response is different than a SurveyResponse since it comes while a user is not authenticated,
 * and therefore never should be resumed, and so doesn't need snapshots -- it can only be created once and then
 * the data is never updated.
 * However, if the user goes on to register, this response should be linked to the user/enrollee, so that the data
 * can be referenced elsewhere.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PreregistrationResponse extends BaseEntity {
    private UUID enrolleeId;
    private UUID creatingParticipantUserId;
    private UUID surveyId;
    private UUID studyEnvironmentId;
    private String fullData;
}
