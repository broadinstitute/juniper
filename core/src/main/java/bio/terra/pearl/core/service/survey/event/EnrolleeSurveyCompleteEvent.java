package bio.terra.pearl.core.service.survey.event;

import bio.terra.pearl.core.model.survey.SurveyResponse;
import lombok.experimental.SuperBuilder;

/** fired when an enrollee completes a survey */
@SuperBuilder
public class EnrolleeSurveyCompleteEvent extends EnrolleeSurveyEvent {
}
