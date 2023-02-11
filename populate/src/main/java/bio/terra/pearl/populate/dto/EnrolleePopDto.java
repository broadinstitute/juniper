package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.populate.dto.consent.ConsentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import java.util.HashSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class EnrolleePopDto extends Enrollee {
    private String linkedUsername;
    private PreEnrollmentResponsePopDto preEnrollmentResponseDto;

    private Set<SurveyResponsePopDto> surveyResponseDtos = new HashSet<>();
    private Set<ConsentResponsePopDto> consentResponseDtos = new HashSet<>();
    private Set<ParticipantTaskPopDto> participantTaskDtos = new HashSet<>();
}
