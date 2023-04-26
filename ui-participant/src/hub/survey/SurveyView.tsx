import React, { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'

import Api, {
  Enrollee,
  Portal,
  StudyEnvironmentSurvey,
  Survey,
  SurveyJsResumeData,
  SurveyResponse,
  SurveyWithResponse
} from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import {
  getResumeData,
  getSurveyJsAnswerList,
  getUpdatedAnswers,
  makeSurveyJsData,
  PageNumberControl,
  useRoutablePageNumber,
  useSurveyJSModel
} from 'util/surveyJsUtils'
import { HubUpdate } from 'hub/hubUpdates'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { withErrorBoundary } from '../../util/ErrorBoundary'
import SurveyReviewModeButton from './ReviewModeButton'
import { Markdown } from '../../landing/Markdown'

const TASK_ID_PARAM = 'taskId'
const AUTO_SAVE_INTERVAL = 3 * 1000  // auto-save every 3 seconds if there are changes

/**
 * display a single survey form to a participant.
 */
function RawSurveyView({ form, enrollee, resumableData, pager, studyShortcode, taskId, activeResponse }:
                         {
                           form: Survey, enrollee: Enrollee, taskId: string, activeResponse?: SurveyResponse,
                           resumableData: SurveyJsResumeData | null, pager: PageNumberControl, studyShortcode: string
                         }) {
  const navigate = useNavigate()
  const { updateEnrollee } = useUser()
  const prevSave = useRef(resumableData?.data ?? {})

  /** Submit the response to the server */
  const onComplete = () => {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const responseDto = {
      resumeData: getResumeData(surveyModel, enrollee.participantUserId),
      enrolleeId: enrollee.id,
      // submitting re-saves the entire form.  This is as insurance against any answers getting lost or misrepresented
      // in the diffing process
      answers: getSurveyJsAnswerList(surveyModel),
      creatingParticipantId: enrollee.participantUserId,
      surveyId: form.id,
      complete: true
    } as SurveyResponse

    Api.submitSurveyResponse({
      studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
      version: form.version, response: responseDto, taskId
    }).then(response => {
      response.enrollee.participantTasks = response.tasks
      response.enrollee.profile = response.profile
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

  const { surveyModel, refreshSurvey, setSurveyModel } = useSurveyJSModel(form, resumableData,
    onComplete, pager, enrollee.profile)

  const saveDiff = () => {
    // we use setSurveyModel to make sure we have the latest version of it, we're not updating it
    setSurveyModel(freshSurveyModel => {
      if (freshSurveyModel) {
        const updatedAnswers = getUpdatedAnswers(prevSave.current as Record<string, object>, freshSurveyModel.data)
        if (updatedAnswers.length < 1) {
          // don't bother saving if there are no changes
          return freshSurveyModel
        }
        const prevPrevSave = prevSave.current
        prevSave.current = freshSurveyModel.data

        const responseDto = {
          resumeData: getResumeData(freshSurveyModel, enrollee.participantUserId),
          enrolleeId: enrollee.id,
          answers: updatedAnswers,
          creatingParticipantId: enrollee.participantUserId,
          surveyId: form.id,
          complete: activeResponse?.complete ?? false
        } as SurveyResponse
        Api.submitSurveyResponse({
          studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
          version: form.version, response: responseDto, taskId
        }).then(() => {
          // no-op for now.  When we implement live-sync, it will be here.
        }).catch(() => {
          // if the operation fails, restore the state from before so the next diff operation will capture the changes
          // that failed to save this time
          prevSave.current = prevPrevSave
        })
      }
      return freshSurveyModel
    })
  }

  useEffect(() => {
    let timeoutHandle: number
    // auto-save the survey at the specified interval
    (function loop() {
      timeoutHandle = window.setTimeout(() => {
        saveDiff()
        loop()
      }, AUTO_SAVE_INTERVAL)
    })()
    return () => {
      window.clearTimeout(timeoutHandle)
    }
  }, [])

  // f3f3f3 background is to match surveyJs "modern" theme
  return <div style={{ background: '#f3f3f3' }} className="survey-js-survey">
    <SurveyReviewModeButton surveyModel={surveyModel}/>
    <h1 className="text-center mt-5 mb-0 pb-0 fw-bold">{form.name}</h1>
    {surveyModel && <SurveyComponent model={surveyModel}/>}
    {form.footer && <div className="container p-3 mb-4 text-muted
    " style={{ marginTop: '-60px', maxWidth: '600px' }}>
      <Markdown>{form.footer}</Markdown>
    </div>}
  </div>
}


/** handles paging the form */
function PagedSurveyView({ form, activeResponse, enrollee, studyShortcode, taskId }:
                           {
                             form: StudyEnvironmentSurvey, activeResponse?: SurveyResponse, enrollee: Enrollee,
                             studyShortcode: string, taskId: string
                           }) {
  const resumableData = makeSurveyJsData(activeResponse?.resumeData,
    activeResponse?.answers, enrollee.participantUserId)

  const pager = useRoutablePageNumber()

  return <RawSurveyView enrollee={enrollee} form={form.survey} taskId={taskId} activeResponse={activeResponse}
    resumableData={resumableData} pager={pager} studyShortcode={studyShortcode}/>
}

/** handles loading the survey form and responses from the server */
function SurveyView() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const [formAndResponses, setFormAndResponse] = useState<SurveyWithResponse | null>(null)
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode

  const [searchParams] = useSearchParams()
  const taskId = searchParams.get(TASK_ID_PARAM) ?? ''

  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  useEffect(() => {
    Api.fetchSurveyAndResponse({
      studyShortcode,
      enrolleeShortcode: enrollee.shortcode, stableId, version, taskId
    })
      .then(response => {
        setFormAndResponse(response)
      }).catch(() => {
        alert('error loading survey form - please retry')
      })
  }, [])

  if (!formAndResponses) {
    return <PageLoadingIndicator/>
  }

  return (
    <PagedSurveyView
      enrollee={enrollee}
      form={formAndResponses.studyEnvironmentSurvey}
      activeResponse={formAndResponses.surveyResponse}
      studyShortcode={studyShortcode}
      taskId={taskId}
    />
  )
}

export default withErrorBoundary(SurveyView)

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
