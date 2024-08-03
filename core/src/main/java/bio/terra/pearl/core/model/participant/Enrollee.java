package bio.terra.pearl.core.model.participant;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private UUID preEnrollmentResponseId;
    private PreEnrollmentResponse preEnrollmentResponse;
    private String shortcode;
    @Builder.Default
    private boolean subject = true; // whether this Enrollee is a primary subject of the study (as opposed to just a proxy or family member)
    private boolean consented;

    @Builder.Default
    private List<FamilyEnrollee> familyEnrollees = new ArrayList<>();
    @Builder.Default
    private List<SurveyResponse> surveyResponses = new ArrayList<>();
    @Builder.Default
    private List<ParticipantTask> participantTasks = new ArrayList<>();
    @Builder.Default
    private List<ParticipantNote> participantNotes = new ArrayList<>();
    @Builder.Default
    private List<KitRequestDto> kitRequests = new ArrayList<>();
    @Builder.Default
    private List<EnrolleeRelation> relations = new ArrayList<>();
}
