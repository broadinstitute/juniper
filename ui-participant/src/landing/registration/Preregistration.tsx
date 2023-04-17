import React from 'react'
import Api, { PreregistrationResponse, Survey } from 'api/api'
import { getAnswerList, getResumeData, useSurveyJSModel } from 'util/surveyJsUtils'
import { RegistrationContextT } from './PortalRegistrationRouter'
import { useNavigate } from 'react-router-dom'

/** Renders a preregistration form, and handles submitting the user-inputted response */
export default function PreRegistration({ registrationContext }: { registrationContext: RegistrationContextT }) {
  const { preRegSurvey, updatePreRegResponseId } = registrationContext
  const navigate = useNavigate()
  const survey = preRegSurvey as Survey
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey, SurveyComponent } =
    useSurveyJSModel(survey, null, handleComplete, pager)

  /** submit the form */
  function handleComplete() {
    if (!surveyModel) {
      return
    }
    const responseDto = {
      resumeData: getResumeData(surveyModel, null),
      fullData: JSON.stringify(getAnswerList(surveyModel)),
      creatingParticipantId: null,
      surveyId: survey.id,
      qualified: surveyModel.getCalculatedValueByName('qualified').value
    } as PreregistrationResponse
    const qualified = surveyModel.getCalculatedValueByName('qualified').value
    const preRegResponse = { ...responseDto, qualified } as PreregistrationResponse
    // submit the form even if it isn't eligible, so we can track stats on exclusions
    Api.submitPreRegResponse({
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preRegResponse
    }).then(result => {
      if (!qualified) {
        updatePreRegResponseId(null)
        navigate('../ineligible')
      } else {
        updatePreRegResponseId(result.id as string)
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
