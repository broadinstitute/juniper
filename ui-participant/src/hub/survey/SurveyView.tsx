import React, { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import 'survey-core/survey.i18n'

import Api, { Portal, SurveyWithResponse } from 'api/api'

import {
  ApiProvider, Enrollee, EnvironmentName, PagedSurveyView,
  useI18n
} from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { withErrorBoundary } from 'util/ErrorBoundary'
import { DocumentTitle } from 'util/DocumentTitle'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { useUser } from '../../providers/UserProvider'
import { HubUpdate } from '../hubUpdates'

const TASK_ID_PARAM = 'taskId'

/** gets the task ID from the URL */
export const useTaskIdParam = (): string | null => {
  const [searchParams] = useSearchParams()
  return searchParams.get(TASK_ID_PARAM)
}

// /**
//  * display a single survey form to a participant.
//  */
// export function RawSurveyView2({
//   form, enrollee, resumableData, pager, studyShortcode,
//   taskId, activeResponse, showHeaders = true
// }:
//                                 {
//                                   form: Survey,
//                                   enrollee: Enrollee,
//                                   taskId: string,
//                                   activeResponse?: SurveyResponse,
//                                   resumableData: SurveyJsResumeData | null,
//                                   pager: PageNumberControl,
//                                   studyShortcode: string,
//                                   showHeaders?: boolean
//                                 }) {
//   const { selectedLanguage } = useI18n()
//   const navigate = useNavigate()
//   const { updateEnrollee, updateProfile } = useUser()
//   const prevSave = useRef(resumableData?.data ?? {})
//   const lastAutoSaveErrored = useRef(false)
//   const { i18n } = useI18n()
//   const foo = usePortalEnv()
//   const studyEnvParams = {
//     studyShortcode, portalShortcode: foo.portal.shortcode,
//     envName: foo.portalEnv.environmentName as EnvironmentName
//   }
//
//   /** Submit the response to the server */
//   const onComplete = async () => {
//     if (!surveyModel || !refreshSurvey) {
//       return
//     }
//     const currentModelValues = getDataWithCalculatedValues(surveyModel)
//     const responseDto = {
//       resumeData: getResumeData(surveyModel, enrollee.participantUserId, true),
//       enrolleeId: enrollee.id,
//       answers: getUpdatedAnswers(prevSave.current as Record<string, object>, currentModelValues, selectedLanguage),
//       creatingParticipantId: enrollee.participantUserId,
//       surveyId: form.id,
//       complete: true
//     } as SurveyResponse
//
//     try {
//       const response = await Api.updateSurveyResponse({
//         studyEnvParams,
//         stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
//         version: form.version, response: responseDto, taskId, alertErrors: true
//       })
//       response.enrollee.participantTasks = response.tasks
//       const hubUpdate: HubUpdate = {
//         message: {
//           title: `${form.name} completed`,
//           type: 'SUCCESS'
//         }
//       }
//       await updateEnrollee(response.enrollee)
//       await updateProfile(response.profile)
//       navigate('/hub', { state: showHeaders ? hubUpdate : undefined })
//     } catch {
//       refreshSurvey(surveyModel, null)
//     }
//   }
//
//   const { surveyModel, refreshSurvey } = useSurveyJSModel(form, resumableData,
//     onComplete, pager)
//
//   /** if the survey has been updated, save the updated answers. */
//   const saveDiff = () => {
//     const currentModelValues = getDataWithCalculatedValues(surveyModel)
//     const updatedAnswers = getUpdatedAnswers(
//       prevSave.current as Record<string, object>, currentModelValues, selectedLanguage)
//     if (updatedAnswers.length < 1) {
//       // don't bother saving if there are no changes
//       return
//     }
//     const prevPrevSave = prevSave.current
//     prevSave.current = currentModelValues
//
//     const responseDto = {
//       resumeData: getResumeData(surveyModel, enrollee.participantUserId),
//       enrolleeId: enrollee.id,
//       answers: updatedAnswers,
//       creatingParticipantId: enrollee.participantUserId,
//       surveyId: form.id,
//       complete: activeResponse?.complete ?? false
//     } as SurveyResponse
//     // only log & alert if this is the first autosave problem to avoid spamming logs & alerts
//     const alertErrors = !lastAutoSaveErrored.current
//     Api.updateSurveyResponse({
//       studyEnvParams, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
//       version: form.version, response: responseDto, taskId, alertErrors
//     }).then(response => {
//       const updatedEnrollee = {
//         ...response.enrollee,
//         participantTasks: response.tasks,
//         profile: response.profile
//       }
//       /**
//        * CAREFUL -- we're updating the enrollee object so that if they navigate back to the dashboard, they'll
//        * see this survey as 'in progress' and capture any profile changes.
//        * However, we don't want to trigger a rerender, because that will wipe out any answers that the user has
//        * typed but are still in focus.  SurveyJS does not write answers to data/state until the question loses focus.
//        * So we use a 'updateWithoutRerender' flag on update Enrollee, this works since there are no currently
//        * visible components that use the enrollee object--otherwise they would not be refreshed
//        */
//       updateEnrollee(updatedEnrollee, true)
//       lastAutoSaveErrored.current = false
//     }).catch(() => {
//       // if the operation fails, restore the state from before so the next diff operation will capture the changes
//       // that failed to save this time
//       prevSave.current = prevPrevSave
//       lastAutoSaveErrored.current = true
//     })
//   }
//
//   // useAutosaveEffect(saveDiff, AUTO_SAVE_INTERVAL)
//
//   surveyModel.locale = selectedLanguage || 'default'
//
//   return (
//     <>
//       <DocumentTitle title={i18n(`${form.stableId}:${form.version}`, form.name)}/>
//       {/* f3f3f3 background is to match surveyJs "modern" theme */}
//       <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
//         {showHeaders && <SurveyReviewModeButton surveyModel={surveyModel}/>}
//         {showHeaders && <SurveyAutoCompleteButton surveyModel={surveyModel}/>}
//         {showHeaders && <h1 className="text-center mt-5 mb-0 pb-0 fw-bold">
//           {i18n(`${form.stableId}:${form.version}`, form.name)}
//         </h1>}
//         <SurveyComponent model={surveyModel}/>
//         <SurveyFooter survey={form} surveyModel={surveyModel}/>
//       </div>
//     </>
//   )
// }
//
//
// /** handles paging the form */
// export function PagedSurveyView2({
//   form, activeResponse, enrollee, studyShortcode, taskId, showHeaders = true
// }:
//                                   {
//                                     form: StudyEnvironmentSurvey,
//                                     activeResponse?: SurveyResponse,
//                                     enrollee: Enrollee,
//                                     studyShortcode: string,
//                                     taskId: string,
//                                     autoSaveInterval?: number,
//                                     showHeaders?: boolean
//                                   }) {
//   const resumableData = makeSurveyJsData(activeResponse?.resumeData,
//     activeResponse?.answers, enrollee.participantUserId)
//
//   const pager = useRoutablePageNumber()
//
//   return <RawSurveyView2 enrollee={enrollee} form={form.survey} taskId={taskId} activeResponse={activeResponse}
//     resumableData={resumableData} pager={pager} studyShortcode={studyShortcode}
//     showHeaders={showHeaders}/>
// }

/** handles loading the survey form and responses from the server */
function SurveyView({ showHeaders = true }: { showHeaders?: boolean }) {
  const { portal, portalEnv } = usePortalEnv()
  const { enrollees } = useActiveUser()
  const { updateEnrollee, updateProfile } = useUser()
  const [formAndResponses, setFormAndResponse] = useState<SurveyWithResponse | null>(null)
  const params = useParams()
  const studyShortcode = params.studyShortcode
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')

  const { i18n, selectedLanguage } = useI18n()
  const taskId = useTaskIdParam() ?? ''
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
              formName: i18n(`${form.stableId}:${form.version}`)
            },
            defaultValue: form.name
          }),
        type: 'SUCCESS'
      }
    }
    navigate('/hub', { state: showHeaders ? hubUpdate : undefined })
  }

  const onFailure = () => {
    // do nothing
  }

  return (
    <ApiProvider api={Api}>
      <DocumentTitle title={i18n(`${form.stableId}:${form.version}`, { defaultValue: form.name })}/>
      <PagedSurveyView
        enrollee={enrollee}
        form={formAndResponses.studyEnvironmentSurvey.survey}
        response={formAndResponses.surveyResponse}
        updateEnrollee={updateEnrollee}
        updateProfile={updateProfile}
        studyEnvParams={studyEnvParams}
        taskId={taskId}
        selectedLanguage={selectedLanguage}
        adminUserId={null}
        onSuccess={onSuccess}
        onFailure={onFailure}
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
