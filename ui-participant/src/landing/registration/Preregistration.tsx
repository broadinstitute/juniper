import React from 'react'
import Api, {Survey} from 'api/api'
import {DenormalizedPreRegResponse, generateDenormalizedData, SourceType, useSurveyJSModel} from 'util/surveyJsUtils'
import {useRegistrationOutlet} from './PortalRegistrationOutlet'
import {useNavigate} from "react-router-dom";

/** Renders a preregistration form, and handles submitting the user-inputted response */
export default function PreRegistrationView() {
  const {preRegSurvey, updatePreRegResponseId} = useRegistrationOutlet()
  const navigate = useNavigate()
  const survey = preRegSurvey as Survey
  // for now, we assume all pre-screeners are a single page
  const pager = {pageNumber: 0, updatePageNumber: () => 0}
  const {surveyModel, refreshSurvey, SurveyComponent} =
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
    const qualified = surveyModel.getCalculatedValueByName('qualified').value
    const preRegResponse = {...denormedResponse, qualified} as DenormalizedPreRegResponse
    // submit the form even if it isn't eligible, so we can track stats on exclusions
    Api.completePortalPreReg({
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preRegResponse
    }).then(result => {
      if (!qualified) {
        navigate('../ineligible')
      } else {
        updatePreRegResponseId(result.id)
      }
    }).catch(() => {
      alert('an error occurred, please retry')
      updatePreRegResponseId(null)
      // SurveyJS doesn't support "uncompleting" surveys, so we have to reinitialize it
      // (for now we assume prereg is only a single page)
      refreshSurvey(surveyModel.data, 1)
    })
  }

  return <div>
    {SurveyComponent}
  </div>
}
