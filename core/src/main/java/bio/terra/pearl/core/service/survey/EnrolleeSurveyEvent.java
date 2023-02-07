package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.service.workflow.EnrolleeEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
public class EnrolleeSurveyEvent extends EnrolleeEvent {
    private SurveyResponse surveyResponse;
}
