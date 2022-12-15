package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.populate.dto.survey.SurveyResponsePopDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class EnrolleePopDto extends Enrollee {
    private String linkedUsername;

    private Set<SurveyResponsePopDto> surveyResponseDtos;
}
