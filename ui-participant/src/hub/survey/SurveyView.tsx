import React, {
  useEffect,
  useState
} from 'react'
import {
  useNavigate,
  useParams
} from 'react-router-dom'
import 'survey-core/survey.i18n'

import Api, {
  Portal,
  SurveyWithResponse
} from 'api/api'

import {
  ApiProvider,
  Enrollee,
  EnvironmentName,
  PagedSurveyView,
  useI18n,
  useTaskIdParam
} from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { withErrorBoundary } from 'util/ErrorBoundary'
import { DocumentTitle } from 'util/DocumentTitle'
import { useUser } from 'providers/UserProvider'
import { HubUpdate } from 'hub/hubUpdates'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { ThemedSurveyQuestionAddressValidation } from 'components/ThemedSurveyAddressValidation'
import { ReactQuestionFactory } from 'survey-react-ui'
import mixpanel from 'mixpanel-browser'
import { ThemedSurveyQuestionDocumentRequest } from 'components/ThemedDocumentRequest'

// register themed address validation type
ReactQuestionFactory.Instance.registerQuestion('addressvalidation', props => {
  return React.createElement(ThemedSurveyQuestionAddressValidation, props)
})

// register themed document upload
ReactQuestionFactory.Instance.registerQuestion('documentrequest', props => {
  return React.createElement(ThemedSurveyQuestionDocumentRequest, props)
})

/** handles loading the survey form and responses from the server */
function SurveyView({ showHeaders = true }: { showHeaders?: boolean }) {
  const { portal, portalEnv } = usePortalEnv()
  const { enrollees, ppUser } = useActiveUser()
  const { user, updateEnrollee, updateProfile, enrollees: allEnrollees } = useUser()
  const [formAndResponses, setFormAndResponse] = useState<SurveyWithResponse | null>(null)
  const params = useParams()
  const studyShortcode = params.studyShortcode
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')

  const proxyProfile = ppUser?.participantUserId != user?.id ? allEnrollees
    .find(enrollee => enrollee.participantUserId === user?.id && enrollee.profile)
    ?.profile : undefined

  const { i18n, selectedLanguage } = useI18n()
  const { taskId } = useTaskIdParam() ?? ''
  const navigate = useNavigate()

  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  const studyEnvParams = {
    studyShortcode,
    envName: portalEnv.environmentName as EnvironmentName,
    portalShortcode: portal.shortcode
  }

  useEffect(() => {
    Api.fetchSurveyAndResponse({
      studyShortcode,
      enrolleeShortcode: enrollee.shortcode, stableId, version, taskId
    })
      .then(response => {
        setFormAndResponse(response)
      }).catch(() => {
        navigate('/hub')
      })
  }, [])

  if (!formAndResponses) {
    return <PageLoadingIndicator/>
  }

  const form = formAndResponses.studyEnvironmentSurvey.survey

  const onSuccess = () => {
    const hubUpdate: HubUpdate = {
      message: {
        title: i18n(
          'hubUpdateFormSubmittedTitle',
          {
            substitutions: {
              formName: i18n(`${form.stableId}:${form.version}`, { defaultValue: form.name })
            }
          }),
        type: 'SUCCESS'
      }
    }
    mixpanel.track('formCompleted', { form: form.stableId, source: 'surveyView' })
    navigate('/hub', { state: showHeaders ? hubUpdate : undefined })
  }

  const onFailure = () => {
    // do nothing
  }

  return (
    <ApiProvider api={Api}>
      <DocumentTitle title={i18n(`${form.stableId}:${form.version}`, { defaultValue: form.name })}/>
      <PagedSurveyView
        studyEnvParams={studyEnvParams}
        form={formAndResponses.studyEnvironmentSurvey.survey}
        enrollee={enrollee}
        updateResponseMap={() => { /* no-op */ }}
        proxyProfile={proxyProfile}
        response={formAndResponses.surveyResponse}
        referencedAnswers={formAndResponses.referencedAnswers}
        selectedLanguage={selectedLanguage}
        setAutosaveStatus={() => { /* no-op */ }}
        adminUserId={null}
        onSuccess={onSuccess}
        onFailure={onFailure}
        updateEnrollee={updateEnrollee}
        updateProfile={updateProfile}
        taskId={taskId}
        showHeaders={showHeaders}
      />
    </ApiProvider>
  )
}

export default withErrorBoundary(SurveyView)

/** Gets the enrollee object matching the given study */
export function enrolleeForStudy(
  enrollees: Enrollee[],
  studyShortcode: string,
  portal: Portal): Enrollee {
  const studyEnvId = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
    .studyEnvironments[0].id

  const enrollee = enrollees.find(e => e.studyEnvironmentId === studyEnvId)
  if (!enrollee) {
    throw `enrollment not found for ${studyShortcode}`
  }
  return enrollee
}
