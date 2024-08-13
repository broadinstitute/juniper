package bio.terra.pearl.populate.dto.participant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.populate.dto.TimeShiftable;
import bio.terra.pearl.populate.dto.kit.KitRequestPopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class EnrolleePopDto extends Enrollee implements TimeShiftable {
    private String linkedUsername;
    private String linkedUsernameKey; // for enrollees linked to users with key-specified accounts
    /**
     * if true, the data for this enrollee will be processed through service calls as if they were submitted
     * via the API, so all side effects, including tasks and events, will be created.
     * If false, the data for the enrollee will just be stored as-is using CRUD service calls.  the latter is more
     * useful for fine-grained control of tasks and status, the former more useful for quick and accurate synthetic
     * participant creation.
     * */
    @Builder.Default
    private boolean simulateSubmissions = false;
    /** if true, the enrollee will be withdrawn via the withdrawEnrollee method in WithdrawnEnrolleeService after creation */
    @Builder.Default
    private boolean withdrawn = false;
    private PreEnrollmentResponsePopDto preEnrollmentResponseDto;
    @Builder.Default
    private List<SurveyResponsePopDto> surveyResponseDtos = new ArrayList<>();
    @Builder.Default
    private List<ParticipantTaskPopDto> participantTaskDtos = new ArrayList<>();
    @Builder.Default
    private List<NotificationPopDto> notifications = new ArrayList<>();
    @Builder.Default
    private List<ParticipantNotePopDto> participantNoteDtos = new ArrayList<>();
    private Integer submittedHoursAgo;
    @Builder.Default
    private Set<KitRequestPopDto> kitRequestDtos = new HashSet<>();
    @Builder.Default
    private List<ProxyPopDto> proxyPopDtos = new ArrayList<>();
}
