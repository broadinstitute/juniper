package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.participant.Profile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SurveyResponseDto {
    private SurveyResponse surveyResponse;
    private String justification;
}
