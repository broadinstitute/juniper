import React, {useState} from 'react'
import {Survey as SurveyComponent} from 'survey-react-ui'
import {generateDenormalizedData, SourceType, useSurveyJSModel} from 'util/surveyJsUtils'
import Api, {Survey} from 'api/api'
import {useRegistrationOutlet} from './PortalRegistrationOutlet'
import {useUser} from "../../providers/UserProvider";
import {useNavigate} from "react-router-dom";

/** This registration survey is a hardcoded temporary survey until we have MS B2C integration. */
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
          'name': 'reg_firstName',
          'title': 'First name',
          'isRequired': true,
          'placeholder': 'your first name'
        },
        {
          'type': 'text',
          'name': 'reg_lastName',
          'title': 'Last name',
          'isRequired': true,
          'placeholder': 'your last name'
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

/** show the participant registration page */
export default function Registration() {
  const {preRegResponseId, updatePreRegResponseId} = useRegistrationOutlet()
  // for now, assume registration surveys are a single page
  const pager = {pageNumber: 0, updatePageNumber: () => 0}
  const {surveyModel, refreshSurvey} = useSurveyJSModel(registrationSurveyModel, null, onComplete, pager)
  const [isRegistered, setIsRegistered] = useState(false)
  const {loginUser} = useUser()
  const navigate = useNavigate()

  /** submit the response */
  function onComplete() {
    if (!surveyModel) {
      return
    }
    const denormedResponse = generateDenormalizedData({
      survey: registrationSurveyModel, surveyJSModel: surveyModel, participantShortcode: 'ANON',
      sourceShortcode: 'ANON', sourceType: SourceType.ANON
    })
    const resumeData = surveyModel?.data
    Api.register({
      preRegResponseId: preRegResponseId as string,
      fullData: denormedResponse
    })
      .then(response => {
        updatePreRegResponseId(null)
        setIsRegistered(true)
        loginUser(response.participantUser)
        navigate('/hub')
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
