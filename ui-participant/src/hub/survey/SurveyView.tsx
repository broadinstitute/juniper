import React, { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'

import Api, {
  Enrollee,
  Portal,
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse,
  SurveyWithResponse
} from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import {
  getDataWithCalculatedValues,
  getResumeData,
  getUpdatedAnswers,
  PageNumberControl,
  useRoutablePageNumber,
  useSurveyJSModel
} from 'util/surveyJsUtils'
import { makeSurveyJsData, SurveyJsResumeData, Markdown, useAutosaveEffect } from '@juniper/ui-core'
import { HubUpdate } from 'hub/hubUpdates'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { withErrorBoundary } from 'util/ErrorBoundary'
import SurveyReviewModeButton from './ReviewModeButton'
import { SurveyModel } from 'survey-core'
import { DocumentTitle } from 'util/DocumentTitle'

const TASK_ID_PARAM = 'taskId'
const AUTO_SAVE_INTERVAL = 3 * 1000  // auto-save every 3 seconds if there are changes

/** gets the task ID from the URL */
export const useTaskIdParam = (): string | null => {
  const [searchParams] = useSearchParams()
  return searchParams.get(TASK_ID_PARAM)
}

/**
 * display a single survey form to a participant.
 */
export function RawSurveyView({
  form, enrollee, resumableData, pager, studyShortcode,
  taskId, activeResponse, showHeaders=true
}:
{
  form: Survey, enrollee: Enrollee, taskId: string, activeResponse?: SurveyResponse,
  resumableData: SurveyJsResumeData | null, pager: PageNumberControl, studyShortcode: string, showHeaders?: boolean
}) {
  const navigate = useNavigate()
  const { selectedLanguage, updateEnrollee } = useUser()
  const { portalEnv } = usePortalEnv()
  const prevSave = useRef(resumableData?.data ?? {})
  const lastAutoSaveErrored = useRef(false)

  /** Submit the response to the server */
  const onComplete = async () => {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const currentModelValues = getDataWithCalculatedValues(surveyModel)
    const responseDto = {
      resumeData: getResumeData(surveyModel, enrollee.participantUserId, true),
      enrolleeId: enrollee.id,
      answers: getUpdatedAnswers(prevSave.current as Record<string, object>,
        currentModelValues, portalEnv.supportedLanguages.find(l =>
          l.languageCode === selectedLanguage)),
      creatingParticipantId: enrollee.participantUserId,
      surveyId: form.id,
      complete: true
    } as SurveyResponse

    try {
      const response = await Api.updateSurveyResponse({
        studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
        version: form.version, response: responseDto, taskId, alertErrors: true
      })
      response.enrollee.participantTasks = response.tasks
      response.enrollee.profile = response.profile
      const hubUpdate: HubUpdate = {
        message: {
          title: `${form.name} completed`,
          type: 'SUCCESS'
        }
      }
      await updateEnrollee(response.enrollee)
      navigate('/hub', { state: showHeaders ? hubUpdate : undefined })
    } catch {
      refreshSurvey(surveyModel, null)
    }
  }

  const { surveyModel, refreshSurvey } = useSurveyJSModel(form, resumableData,
    onComplete, pager, enrollee.profile)

  /** if the survey has been updated, save the updated answers. */
  const saveDiff = () => {
    const currentModelValues = getDataWithCalculatedValues(surveyModel)
    const updatedAnswers = getUpdatedAnswers(
        prevSave.current as Record<string, object>, currentModelValues, portalEnv.supportedLanguages.find(l =>
          l.languageCode === selectedLanguage))
    if (updatedAnswers.length < 1) {
      // don't bother saving if there are no changes
      return
    }
    const prevPrevSave = prevSave.current
    prevSave.current = currentModelValues

    const responseDto = {
      resumeData: getResumeData(surveyModel, enrollee.participantUserId),
      enrolleeId: enrollee.id,
      answers: updatedAnswers,
      creatingParticipantId: enrollee.participantUserId,
      surveyId: form.id,
      complete: activeResponse?.complete ?? false
    } as SurveyResponse
    // only log & alert if this is the first autosave problem to avoid spamming logs & alerts
    const alertErrors =  !lastAutoSaveErrored.current
    Api.updateSurveyResponse({
      studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
      version: form.version, response: responseDto, taskId, alertErrors
    }).then(response => {
      const updatedEnrollee = {
        ...response.enrollee,
        participantTasks: response.tasks,
        profile: response.profile
      }
      /**
       * CAREFUL -- we're updating the enrollee object so that if they navigate back to the dashboard, they'll
       * see this survey as 'in progress' and capture any profile changes.
       * However, we don't want to trigger a rerender, because that will wipe out any answers that the user has
       * typed but are still in focus.  SurveyJS does not write answers to data/state until the question loses focus.
       * So we use a 'updateWithoutRerender' flag on update Enrollee, this works since there are no currently
       * visible components that use the enrollee object--otherwise they would not be refreshed
       */
      updateEnrollee(updatedEnrollee, true)
      lastAutoSaveErrored.current = false
    }).catch(() => {
      // if the operation fails, restore the state from before so the next diff operation will capture the changes
      // that failed to save this time
      prevSave.current = prevPrevSave
      lastAutoSaveErrored.current = true
    })
  }

  useAutosaveEffect(saveDiff, AUTO_SAVE_INTERVAL)

  surveyModel.locale = selectedLanguage

  return (
    <>
      <DocumentTitle title={form.name} />
      {/* f3f3f3 background is to match surveyJs "modern" theme */}
      <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
        { showHeaders && <SurveyReviewModeButton surveyModel={surveyModel}/> }
        { showHeaders && <h1 className="text-center mt-5 mb-0 pb-0 fw-bold">{form.name}</h1> }
        <SurveyComponent model={surveyModel}/>
        <SurveyFooter survey={form} surveyModel={surveyModel}/>
      </div>
    </>
  )
}

/** renders the foot for the survey, if it exists and we are on the last page */
export function SurveyFooter({ survey, surveyModel }: { survey: Survey, surveyModel: SurveyModel }) {
  if (!survey.footer || !surveyModel.isLastPage) {
    return null
  }
  return <div className="p-3 mb-0 w-100 d-flex justify-content-center"
    style={{ background: '#d6d6d6' }}>
    <div style={{ maxWidth: '600px' }}>
      <Markdown>{survey.footer}</Markdown>
    </div>
  </div>
}


/** handles paging the form */
export function PagedSurveyView({
  form, activeResponse, enrollee, studyShortcode, taskId, showHeaders=true
}:
{
  form: StudyEnvironmentSurvey, activeResponse?: SurveyResponse, enrollee: Enrollee,
  studyShortcode: string, taskId: string, autoSaveInterval?: number, showHeaders?: boolean
}) {
  const resumableData = makeSurveyJsData(activeResponse?.resumeData,
    activeResponse?.answers, enrollee.participantUserId)

  const pager = useRoutablePageNumber()

  return <RawSurveyView enrollee={enrollee} form={form.survey} taskId={taskId} activeResponse={activeResponse}
    resumableData={resumableData} pager={pager} studyShortcode={studyShortcode} showHeaders={showHeaders}/>
}

/** handles loading the survey form and responses from the server */
function SurveyView({ showHeaders=true }: {showHeaders?: boolean}) {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const [formAndResponses, setFormAndResponse] = useState<SurveyWithResponse | null>(null)
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode

  const taskId = useTaskIdParam() ?? ''
  const navigate = useNavigate()

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
        navigate('/hub')
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
      showHeaders={showHeaders}
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
