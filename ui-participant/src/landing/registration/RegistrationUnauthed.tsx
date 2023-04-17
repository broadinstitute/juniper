import React from 'react'
import {Survey as SurveyComponent} from 'survey-react-ui'
import {generateFormResponseDto, useSurveyJSModel} from 'util/surveyJsUtils'
import Api, {Survey} from 'api/api'
import {RegistrationContextT} from './PortalRegistrationRouter'
import {useUser} from '../../providers/UserProvider'
import {useNavigate} from 'react-router-dom'

/** This registration survey is a hardcoded survey--will be deprecated soon */
const registrationSurvey = {
  'logoPosition': 'right',
  'pages': [
    {
      'name': 'page1',
      'elements': [
        {
          'type': 'html',
          'name': 'question1',
          /* eslint-disable max-len */
          'html': '<h3 class="mt-5 fw-bold text-center">Create your account</h3>\n<p class="p-4 text-center fs-5"> You will use this information to later sign in to your account.  We will use the email address provided to communicate with you.</p>'
        },
        {
          'type': 'text',
          'name': 'reg_email',
          'title': 'Email address',
          'isRequired': true,
          'inputType': 'email',
          'placeholder': 'name@email.com'
        }
      ]
    }
  ]
}

const registrationSurveyModel: Survey = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Registration',
  stableId: 'pearlDefaultRegistration',
  version: 1,
  content: JSON.stringify(registrationSurvey),
  allowParticipantCompletion: true,
  allowMultipleResponses: false,
  allowParticipantReedit: false
}

/**
 * Show the registration page for internal-only login.  Currently deprecated -- use only in cases where
 * B2C is down, or for automation
 * */
export default function RegistrationUnauthed({registrationContext, returnTo}: {
  registrationContext: RegistrationContextT,
  returnTo: string | null
}) {
  const {preRegResponseId} = registrationContext
  // for now, assume registration surveys are a single page
  const pager = {pageNumber: 0, updatePageNumber: () => 0}
  const {surveyModel, refreshSurvey} = useSurveyJSModel(registrationSurveyModel, null, onComplete, pager)
  const {loginUser} = useUser()
  const navigate = useNavigate()

  /** submit the response */
  function onComplete() {
    if (!surveyModel) {
      return
    }
    const responseDto = generateFormResponseDto({
      surveyJSModel: surveyModel, enrolleeId: null, participantUserId: null
    })
    const resumeData = surveyModel?.data
    Api.internalRegister({
      preRegResponseId: preRegResponseId as string,
      fullData: responseDto
    })
      .then(response => {
        loginUser({user: response.participantUser, enrollees: []}, response.participantUser.token)
        if (returnTo) {
          navigate(returnTo)
        }
      }).catch(() => {
      alert('an error occurred.  Please retry.  If this persists, contact us')
      // if there's an error, reshow the survey (for now, assume registration is a single page)
      refreshSurvey(resumeData, 1)
    })
  }

  return <div>
    {(surveyModel) && <SurveyComponent model={surveyModel}/>}
  </div>
}
