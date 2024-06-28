package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.participant.Profile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/*
 * SurveyResponseWithJustification exists for admin flows, which require a justification
 * to be provided for audit purposes. Participant flows do not require a justification and
 * should rely on SurveyResponse instead.
 */

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SurveyResponseWithJustification extends SurveyResponse {
    private String justification;
}
