import React from 'react'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { generateFormResponseDto, SourceType, useSurveyJSModel } from 'util/surveyJsUtils'
import Api, { Survey } from 'api/api'
import { RegistrationContextT } from './PortalRegistrationRouter'
import { useUser } from '../../providers/UserProvider'
import { useNavigate, useParams } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { useReturnToStudy } from 'state'

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

/** Show the B2C participant registration page */
export default function Registration({ registrationContext, returnTo }: {
  registrationContext: RegistrationContextT,
  returnTo: string | null
}) {
  const auth = useAuth()
  const studyShortcode = useParams().studyShortcode || null
  const [, setReturnToStudy] = useReturnToStudy()

  const register = () => {
    // Remember study for when we come back from B2C,
    // at which point RedirectFromOAuth will complete the registration
    setReturnToStudy(studyShortcode)
    auth.signinRedirect({ extraQueryParams: { option: 'signup' } })
  }

  // TODO: remove legacy internal registration
  return <>
    <p>
      <button type="button" className="btn btn-secondary" onClick={() => register()}>Register</button>
    </p>
    <p>
      For now, the internal registration form also remains below if needed.
    </p>
    <InternalRegistration registrationContext={registrationContext} returnTo={returnTo}/>
  </>
}

/** show the participant registration page */
export function InternalRegistration({ registrationContext, returnTo }: {
  registrationContext: RegistrationContextT,
  returnTo: string | null
}) {
  const { preRegResponseId, updatePreRegResponseId } = registrationContext
  // for now, assume registration surveys are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey } = useSurveyJSModel(registrationSurveyModel, null, onComplete, pager)
  const { loginUser } = useUser()
  const navigate = useNavigate()

  /** submit the response */
  function onComplete() {
    if (!surveyModel) {
      return
    }
    const responseDto = generateFormResponseDto({
      surveyJSModel: surveyModel, enrolleeId: null, sourceType: SourceType.ANON
    })
    const resumeData = surveyModel?.data
    Api.internalRegister({
      preRegResponseId: preRegResponseId as string,
      fullData: responseDto
    })
      .then(response => {
        updatePreRegResponseId(null)
        loginUser({ user: response.participantUser, enrollees: [] })
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
