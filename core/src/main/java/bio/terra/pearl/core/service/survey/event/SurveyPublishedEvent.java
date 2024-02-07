package bio.terra.pearl.core.service.survey.event;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.service.workflow.BaseEvent;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents an update to enrollee survey data, most commonly by the participant submitting a completed survey
 * Eventually we might want separate events for e.g. completing a survey vs. updating one.  But for now we just have one
 */
@Getter @Setter
@SuperBuilder
public class SurveyPublishedEvent extends Event implements BaseEvent {
    private Survey survey;  // the survey that is being published -- the content does not need to be attached
}
