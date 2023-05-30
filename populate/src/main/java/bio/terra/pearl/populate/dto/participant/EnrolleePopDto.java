package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
import bio.terra.pearl.populate.dto.kit.KitRequestDto;
import bio.terra.pearl.populate.dto.notifications.NotificationPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class EnrolleePopDto extends Enrollee {
    private String linkedUsername;
    /**
     * if true, the data for this enrollee will be processed through service calls as if they were submitted
     * via the API, so all side effects, including tasks and events, will be created.
     * If false, the data for the enrollee will just be stored as-is using CRUD service calls.  the latter is more
     * useful for fine-grained control of tasks and status, the former more useful for quick and accurate synthetic
     * participant creation.
     * */
    private boolean simulateSubmissions = false;
    /** if true, the enrollee will be withdrawn via the withdrawEnrollee method in WithdrawnEnrolleeService after creation */
    private boolean withdrawn = false;
    private PreEnrollmentResponsePopDto preEnrollmentResponseDto;

    private Set<SurveyResponsePopDto> surveyResponseDtos = new HashSet<>();
    private Set<ConsentResponsePopDto> consentResponseDtos = new HashSet<>();
    private Set<ParticipantTaskPopDto> participantTaskDtos = new HashSet<>();
    private List<NotificationPopDto> notifications = new ArrayList<>();
    private Integer submittedHoursAgo;
    private Set<KitRequestDto> kitRequestDtos = new HashSet<>();
}
