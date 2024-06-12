package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** event for a participant's status changing to consented */
@Getter
@Setter
@SuperBuilder
public class EnrolleeConsentEvent extends EnrolleeEvent {
}
