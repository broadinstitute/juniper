package bio.terra.pearl.core.model.survey;

import java.util.List;

/** convenience class for grouping together a form and its configuration and most recent/active response */
public record SurveyWithResponse(StudyEnvironmentSurvey studyEnvironmentSurvey,
                                 SurveyResponse surveyResponse,
                                 // if the survey uses answers from other surveys, this will be populated
                                 List<Answer> referencedAnswers) {
}

