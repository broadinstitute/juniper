import React, { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Api, {
  ConsentForm,
  Enrollee,
  Portal,
  ResumableData,
  StudyEnvironmentSurvey,
  SurveyResponse,
  SurveyWithResponse
} from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import {
  generateFormResponseDto,
  PageNumberControl,
  SourceType,
  SurveyResponseDto,
  useRoutablePageNumber,
  useSurveyJSModel
} from 'util/surveyJsUtils'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import LoadingSpinner from 'util/LoadingSpinner'

const TASK_ID_PARAM = 'taskId'

/**
 * display a single survey form to a participant.
 */
function RawSurveyView({ form, enrollee, resumableData, pager, studyShortcode, taskId }:
                         {
                           form: ConsentForm, enrollee: Enrollee, taskId: string
                           resumableData: ResumableData | null, pager: PageNumberControl, studyShortcode: string
                         }) {
  const { surveyModel, pageNumber, refreshSurvey } = useSurveyJSModel(form, resumableData, onComplete, pager)
  const navigate = useNavigate()
  const { updateEnrollee } = useUser()
  if (surveyModel && resumableData) {
    // survey responses aren't yet editable after completion
    surveyModel.mode = 'display'
  }

  /** Submit the response to the server */
  function onComplete() {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const responseDto = generateFormResponseDto({
      surveyJSModel: surveyModel, enrolleeId: enrollee.id, sourceType: SourceType.ENROLLEE
    }) as SurveyResponseDto
    responseDto.complete = true

    Api.submitSurveyResponse({
      studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
      version: form.version, response: responseDto, taskId
    }).then(response => {
      response.enrollee.participantTasks = response.tasks
      updateEnrollee(response.enrollee)
      navigate('/hub', { state: { message: { content: `${form.name} submitted`, messageType: 'success' } } })
    }).catch(() => {
      refreshSurvey(surveyModel, null)
      alert('an error occurred')
    })
  }


  return <div>
    <h4 className="text-center mt-2">{form.name}</h4>
    {surveyModel && <div className="d-flex align-items-center flex-column">
      <span className="detail">page {pageNumber} of {surveyModel.pages.length}</span>
      <SurveyComponent model={surveyModel}/>
    </div>}
  </div>
}

/** handles paging the form */
function PagedSurveyView({ form, activeResponse, enrollee, studyShortcode }:
                           {
                             form: StudyEnvironmentSurvey, activeResponse: SurveyResponse, enrollee: Enrollee,
                             studyShortcode: string
                           }) {
  const [searchParams] = useSearchParams()
  const taskId = searchParams.get(TASK_ID_PARAM) ?? ''

  let resumableData = null
  if (activeResponse?.lastSnapshot) {
    resumableData = JSON.parse(activeResponse?.lastSnapshot.resumeData) as ResumableData
  }

  const pager = useRoutablePageNumber()

  return <RawSurveyView enrollee={enrollee} form={form.survey} taskId={taskId}
    resumableData={resumableData} pager={pager} studyShortcode={studyShortcode}/>
}

/** handles loading the consent form and responses from the server */
export default function SurveyView() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const [formAndResponses, setFormAndResponse] = useState<SurveyWithResponse | null>(null)
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode
  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  useEffect(() => {
    Api.fetchSurveyAndResponse({
      studyShortcode,
      enrolleeShortcode: enrollee.shortcode, stableId, version, taskId: null
    })
      .then(response => {
        setFormAndResponse(response)
      }).catch(() => {
        alert('error loading consent form - please retry')
      })
  }, [])

  return <LoadingSpinner isLoading={!formAndResponses}>
    {formAndResponses && <PagedSurveyView enrollee={enrollee} form={formAndResponses.studyEnvironmentSurvey}
      activeResponse={formAndResponses.surveyResponse}
      studyShortcode={studyShortcode}/>}
  </LoadingSpinner>
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

