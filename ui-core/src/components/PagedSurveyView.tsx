import { Survey, SurveyResponse } from 'src/types/forms'
import { useApiContext } from '../participant/ApiProvider'
import {
  getDataWithCalculatedValues,
  getResumeData,
  getUpdatedAnswers,
  makeSurveyJsData,
  PageNumberControl,
  SurveyFooter,
  SurveyJsResumeData,
  useRoutablePageNumber,
  useSurveyJSModel
} from '../surveyUtils'
import { Survey as SurveyComponent } from 'survey-react-ui'
import React, { useRef } from 'react'
import { useAutosaveEffect } from '../autoSaveUtils'
import { useI18n } from '../participant/I18nProvider'
import { SurveyAutoCompleteButton } from '../components/SurveyAutoCompleteButton'
import { SurveyReviewModeButton } from '../components/ReviewModeButton'
import { StudyEnvParams } from 'src/types/study'
import { Enrollee } from 'src/types/user'
import { Profile } from 'src/types/address'

const AUTO_SAVE_INTERVAL = 3 * 1000  // auto-save every 3 seconds if there are changes

/** handles paging the form */
export function PagedSurveyView({
  studyEnvParams, form, response, updateEnrollee, updateProfile, taskId, selectedLanguage,
  enrollee, adminUserId, onSuccess, onFailure, showHeaders = true
}: {
    studyEnvParams: StudyEnvParams, form: Survey, response: SurveyResponse,
    onSuccess: () => void, onFailure: () => void,
    selectedLanguage: string,
    updateEnrollee: (enrollee: Enrollee, updateWithoutRerender?: boolean) => void,
    updateProfile: (profile: Profile, updateWithoutRerender?: boolean) => void,
    taskId: string, adminUserId: string | null, enrollee: Enrollee, showHeaders?: boolean,
}) {
  const resumableData = makeSurveyJsData(response?.resumeData, response?.answers, enrollee.participantUserId)
  const pager = useRoutablePageNumber()

  return <RawSurveyView studyEnvParams={studyEnvParams} form={form} taskId={taskId} updateEnrollee={updateEnrollee}
    updateProfile={updateProfile}
    resumableData={resumableData} pager={pager} enrollee={enrollee} onSuccess={onSuccess} onFailure={onFailure}
    selectedLanguage={selectedLanguage} adminUserId={adminUserId} showHeaders={showHeaders}/>
}

/**
 * display a single survey form to a participant.
 */
export function RawSurveyView({
  studyEnvParams, form, resumableData, pager, updateEnrollee,
  updateProfile, onSuccess, onFailure, selectedLanguage,
  taskId, response, adminUserId, enrollee, showHeaders = true
}: {
  studyEnvParams: StudyEnvParams,
  form: Survey,
  taskId: string,
  selectedLanguage: string,
  response?: SurveyResponse,
  adminUserId: string | null,
  updateEnrollee: (enrollee: Enrollee, updateWithoutRerender?: boolean) => void,
  updateProfile: (profile: Profile, updateWithoutRerender?: boolean) => void,
  onSuccess: () => void,
  onFailure: () => void,
  resumableData: SurveyJsResumeData | null,
  pager: PageNumberControl,
  enrollee: Enrollee,
  showHeaders?: boolean
}) {
  const Api = useApiContext()
  const { i18n } = useI18n()
  const prevSave = useRef(resumableData?.data ?? {})
  const lastAutoSaveErrored = useRef(false)

  /** Submit the response to the server */
  const onComplete = async () => {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const currentModelValues = getDataWithCalculatedValues(surveyModel)
    const responseDto = {
      resumeData: getResumeData(surveyModel, adminUserId, true),
      enrolleeId: enrollee.id,
      answers: getUpdatedAnswers(resumableData || {}, currentModelValues, selectedLanguage),
      creatingParticipantId: adminUserId ? null : enrollee.participantUserId,
      creatingAdminUserId: adminUserId,
      surveyId: form.id,
      complete: true
    } as SurveyResponse

    try {
      const response = await Api.updateSurveyResponse({
        studyEnvParams, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
        version: form.version, response: responseDto, taskId
      })
      response.enrollee.participantTasks = response.tasks
      await updateEnrollee(response.enrollee)
      await updateProfile(response.profile)
      refreshSurvey(surveyModel, null)
      onSuccess()
    } catch {
      onFailure()
      refreshSurvey(surveyModel, null)
    }
  }

  const { surveyModel, refreshSurvey } = useSurveyJSModel(form, resumableData,
    onComplete, pager, studyEnvParams.envName, enrollee.profile)

  surveyModel.locale = selectedLanguage

  const saveDiff = () => {
    const currentModelValues = getDataWithCalculatedValues(surveyModel)
    const updatedAnswers = getUpdatedAnswers(
      prevSave.current as Record<string, object>, currentModelValues, selectedLanguage)
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
      complete: response?.complete ?? false
    } as SurveyResponse
    // only log & alert if this is the first autosave problem to avoid spamming logs & alerts
    const alertErrors = !lastAutoSaveErrored.current
    Api.updateSurveyResponse({
      studyEnvParams, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
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

  return (
    <>
      {/* f3f3f3 background is to match surveyJs "modern" theme */}
      <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
        {showHeaders && <SurveyReviewModeButton surveyModel={surveyModel} envName={studyEnvParams.envName}/>}
        {showHeaders && <SurveyAutoCompleteButton surveyModel={surveyModel} envName={studyEnvParams.envName}/>}
        {showHeaders && <h1 className="text-center mt-5 mb-0 pb-0 fw-bold">
          {i18n(`${form.stableId}:${form.version}`, { defaultValue: form.name })}
        </h1>}
        <SurveyComponent model={surveyModel}/>
        <SurveyFooter survey={form} surveyModel={surveyModel}/>
      </div>
    </>
  )
}
