import React from 'react'
import Api from 'api/api'
import { DenormalizedPreRegResponse, generateDenormalizedData, SourceType, useSurveyJSModel } from 'util/surveyJsUtils'
import { useRegistrationOutlet } from './RegistrationOutlet'

/** Renders a preregistration form, and handles submitting the user-inputted response */
export default function PreRegistrationView() {
  const { portalShortcode, studyShortcode, studyEnv, updatePreRegResponseId } = useRegistrationOutlet()
  const survey = studyEnv.preRegSurvey
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey, SurveyComponent } =
    useSurveyJSModel(survey, null, handleComplete, pager)

  /** submit the form */
  function handleComplete() {
    if (!surveyModel) {
      return
    }
    const denormedResponse = generateDenormalizedData({
      survey, surveyJSModel: surveyModel, participantShortcode: 'ANON',
      sourceShortcode: 'ANON', sourceType: SourceType.ANON
    })
    // for now, we assume the survey is constructed so that it cannot be submitted with invalid/incomplete answers
    const preRegResponse = { ...denormedResponse, qualified: true } as DenormalizedPreRegResponse
    Api.completePreReg({
      portalShortcode,
      envName: studyEnv.environmentName,
      studyShortcode,
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preRegResponse
    }).then(result => {
      updatePreRegResponseId(result.id)
    }).catch(() => {
      alert('an error occurred, please retry')
      updatePreRegResponseId(null)
      // SurveyJS doesn't support "uncompleting" surveys, so we have to reinitialize it
      // (for now we assume prereg is only a single page)
      refreshSurvey(surveyModel.data, 1)
    })
  }

  return <div>
    { SurveyComponent }
  </div>
}
