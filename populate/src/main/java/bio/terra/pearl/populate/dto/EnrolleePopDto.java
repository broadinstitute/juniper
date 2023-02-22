package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
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
    private PreEnrollmentResponsePopDto preEnrollmentResponseDto;

    private Set<SurveyResponsePopDto> surveyResponseDtos = new HashSet<>();
    private Set<ConsentResponsePopDto> consentResponseDtos = new HashSet<>();
    private Set<ParticipantTaskPopDto> participantTaskDtos = new HashSet<>();
    private List<NotificationPopDto> notifications = new ArrayList<>();
}
