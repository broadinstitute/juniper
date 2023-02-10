package bio.terra.pearl.core.model.survey;

/** convenience class for grouping together a form and its configuration and most recent/active response */
public record SurveyWithResponse(StudyEnvironmentSurvey studyEnvironmentSurvey,
                                   SurveyResponse surveyResponse) {
}

