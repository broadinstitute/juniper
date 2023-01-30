package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Enrollee is essentially a holder for any study-specific data for a user.   An Enrollee object is created as soon
 * as a participant indicates interest and qualifies for a study.
 * Note that an "Enrollee" might not be classified as "Enrolled" for a given study.
 * So, e.g. an "Enrollee" might not be consented yet.
 */
@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Enrollee extends BaseEntity {
    private UUID participantUserId;
    private UUID profileId;
    private Profile profile;
    private UUID studyEnvironmentId;
    private String shortcode;
    @Builder.Default
    private boolean withdrawn = false;
    @Builder.Default
    private Set<SurveyResponse> surveyResponses = new HashSet<>();
    @Builder.Default
    private Set<ConsentResponse> consentResponses = new HashSet<>();
    @Builder.Default
    private Set<ParticipantTask> participantTasks = new HashSet<>();
}
