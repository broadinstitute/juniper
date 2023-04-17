import React, { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Api, {
  ConsentForm,
  ConsentResponse,
  ConsentWithResponses,
  Enrollee,
  Portal,
  StudyEnvironmentConsent,
  SurveyJsResumeData
} from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import {
  ConsentResponseDto,
  generateFormResponseDto,
  PageNumberControl,
  useRoutablePageNumber,
  useSurveyJSModel
} from 'util/surveyJsUtils'
import { HubUpdate } from 'hub/hubUpdates'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { PageLoadingIndicator } from 'util/LoadingSpinner'

const TASK_ID_PARAM = 'taskId'

/**
 * display a single consent form to a participant.  The pageNumber argument can be specified to start at the given
 * page
 */
function RawConsentView({ form, enrollee, resumableData, pager, studyShortcode, taskId }:
                          {
                            form: ConsentForm, enrollee: Enrollee, taskId: string
                            resumableData: SurveyJsResumeData | null, pager: PageNumberControl, studyShortcode: string
                          }) {
  const { surveyModel, pageNumber, refreshSurvey } = useSurveyJSModel(form, resumableData, onComplete, pager)
  const navigate = useNavigate()
  const { updateEnrollee } = useUser()
  if (surveyModel && resumableData) {
    // consent responses are not editable -- they must be withdrawn via separate workflow
    surveyModel.mode = 'display'
  }

  /** Submit the response to the server */
  function onComplete() {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const consentResponseDto = generateFormResponseDto({
      surveyJSModel: surveyModel, enrolleeId: enrollee.id, participantUserId: enrollee.participantUserId
    }) as ConsentResponseDto
    // if the form doesn't export an explicit "consented" property, then the default is that they've consented
    // if they are able to submit it
    const consented = surveyModel.getCalculatedValueByName('consented')?.value ?? true
    consentResponseDto.consented = consented
    consentResponseDto.consentFormId = form.id

    Api.submitConsentResponse({
      studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
      version: form.version, response: consentResponseDto, taskId
    }).then(response => {
      response.enrollee.participantTasks = response.tasks
      updateEnrollee(response.enrollee)
      const hubUpdate: HubUpdate = {
        message: {
          title: `${form.name} completed`,
          type: 'success'
        }
      }
      navigate('/hub', { state: hubUpdate })
    }).catch(() => {
      refreshSurvey(surveyModel, null)
      alert('an error occurred')
    })
  }


  return <div>
    <h1 className="h4 text-center mt-2">{form.name}</h1>
    {surveyModel && <div className="d-flex align-items-center flex-column">
      <span className="detail">page {pageNumber} of {surveyModel.pages.length}</span>
      <SurveyComponent model={surveyModel}/>
    </div>}
  </div>
}

/** handles paging the form */
function PagedConsentView({ form, responses, enrollee, studyShortcode }:
                            {
                              form: StudyEnvironmentConsent, responses: ConsentResponse[], enrollee: Enrollee,
                              studyShortcode: string
                            }) {
  const [searchParams] = useSearchParams()
  const taskId = searchParams.get(TASK_ID_PARAM) ?? ''

  const response = responses[0]

  let resumableData = null
  if (response?.resumeData) {
    resumableData = JSON.parse(response?.resumeData) as SurveyJsResumeData
  }

  const pager = useRoutablePageNumber()

  return <RawConsentView enrollee={enrollee} form={form.consentForm} taskId={taskId}
    resumableData={resumableData} pager={pager} studyShortcode={studyShortcode}/>
}

/** handles loading the consent form and responses from the server */
export default function ConsentView() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const [formAndResponses, setFormAndResponses] = useState<ConsentWithResponses | null>(null)
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode
  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  useEffect(() => {
    Api.fetchConsentAndResponses({
      studyShortcode,
      enrolleeShortcode: enrollee.shortcode, stableId, version, taskId: null
    })
      .then(response => {
        setFormAndResponses(response)
      }).catch(() => {
        alert('error loading consent form - please retry')
      })
  }, [])

  if (!formAndResponses) {
    return <PageLoadingIndicator/>
  }

  return (
    <PagedConsentView
      enrollee={enrollee}
      form={formAndResponses.studyEnvironmentConsent}
      responses={formAndResponses.consentResponses}
      studyShortcode={studyShortcode}
    />
  )
}

/** Gets the enrollee object matching the given study */
function enrolleeForStudy(enrollees: Enrollee[], studyShortcode: string, portal: Portal): Enrollee {
  const studyEnvId = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
    .studyEnvironments[0].id

  const enrollee = enrollees.find(enrollee => enrollee.studyEnvironmentId === studyEnvId)
  if (!enrollee) {
    throw `enrollment not found for ${studyShortcode}`
  }
  return enrollee
}
