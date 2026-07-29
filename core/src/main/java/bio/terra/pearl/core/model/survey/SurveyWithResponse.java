package bio.terra.pearl.core.model.survey;

import java.util.Map;

/** convenience class for grouping together a form and its configuration and most recent/active response */
public record SurveyWithResponse(StudyEnvironmentSurvey studyEnvironmentSurvey,
                                 SurveyResponse surveyResponse,
                                 // if the survey uses answers from other surveys, this will be populated.
                                 // keyed by the full variable name (e.g. "survey1.question1" or "survey1['otherStudy'].question1")
                                 // so the frontend can set the surveyJS variable directly without needing to reconstruct it
                                 Map<String, Answer> referencedAnswers) {
}

