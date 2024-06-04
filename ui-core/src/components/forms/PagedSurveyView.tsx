import { Survey, SurveyResponse } from 'src/types/forms'
import { useApiContext } from '../../participant/ApiProvider'
import {
  getDataWithCalculatedValues,
  getResumeData,
  getUpdatedAnswers,
  makeSurveyJsData,
  SurveyFooter,
  useRoutablePageNumber,
  useSurveyJSModel
} from '../../surveyUtils'
import { Survey as SurveyComponent } from 'survey-react-ui'
import React, { useRef } from 'react'
import { useAutosaveEffect } from '../../autoSaveUtils'
import { useI18n } from '../../participant/I18nProvider'
import { SurveyAutoCompleteButton } from './SurveyAutoCompleteButton'
import { SurveyReviewModeButton } from './ReviewModeButton'
import { StudyEnvParams } from 'src/types/study'
import { Enrollee, Profile } from 'src/types/user'

const AUTO_SAVE_INTERVAL = 3 * 1000  // auto-save every 3 seconds if there are changes

export type AutosaveStatus = 'saving' | 'saved' | 'error'

/** handles paging the form */
export function PagedSurveyView({
  studyEnvParams, form, response, updateEnrollee, updateProfile, taskId, selectedLanguage,
  setAutosaveStatus, enrollee, proxyProfile, adminUserId, onSuccess, onFailure, showHeaders = true
}: {
    studyEnvParams: StudyEnvParams, form: Survey, response: SurveyResponse,
    onSuccess: () => void, onFailure: () => void,
    selectedLanguage: string,
    setAutosaveStatus: (status: AutosaveStatus) => void,
    updateEnrollee: (enrollee: Enrollee, updateWithoutRerender?: boolean) => void,
    updateProfile: (profile: Profile, updateWithoutRerender?: boolean) => void,
    proxyProfile?: Profile,
    taskId: string, adminUserId: string | null, enrollee: Enrollee, showHeaders?: boolean,
}) {
  const resumableData = makeSurveyJsData(response?.resumeData, response?.answers, enrollee.participantUserId)
  const pager = useRoutablePageNumber()

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
      resumeData: getResumeData(surveyModel, adminUserId || enrollee.participantUserId, true),
      enrolleeId: enrollee.id,
      answers: getUpdatedAnswers(prevSave.current as Record<string, object>, currentModelValues, selectedLanguage),
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
      updateEnrollee(response.enrollee)
      updateProfile(response.profile)
      refreshSurvey(surveyModel, null)
      onSuccess()
    } catch {
      onFailure()
      refreshSurvey(surveyModel, null)
    }
  }

  const { surveyModel, refreshSurvey } = useSurveyJSModel(
    form, resumableData, onComplete, pager, studyEnvParams.envName, enrollee.profile, proxyProfile
  )

  surveyModel.locale = selectedLanguage

  const saveDiff = () => {
    const currentModelValues = getDataWithCalculatedValues(surveyModel)
    const updatedAnswers = getUpdatedAnswers(
        prevSave.current as Record<string, object>, currentModelValues, selectedLanguage)
    if (updatedAnswers.length < 1) {
      // don't bother saving if there are no changes
      return
    }
    setAutosaveStatus('saving')
    const prevPrevSave = prevSave.current
    prevSave.current = currentModelValues

    const responseDto = {
      resumeData: getResumeData(surveyModel, adminUserId || enrollee.participantUserId),
      enrolleeId: enrollee.id,
      answers: updatedAnswers,
      creatingParticipantId: adminUserId ? null : enrollee.participantUserId,
      creatingAdminUserId: adminUserId,
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
      setAutosaveStatus('saved')
    }).catch(() => {
      // if the operation fails, restore the state from before so the next diff operation will capture the changes
      // that failed to save this time
      setAutosaveStatus('error')
      prevSave.current = prevPrevSave
      lastAutoSaveErrored.current = true
    })
  }

  useAutosaveEffect(saveDiff, AUTO_SAVE_INTERVAL)

  return (
    <>
      {/* f3f3f3 background is to match surveyJs "modern" theme */}
      <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
        <SurveyReviewModeButton surveyModel={surveyModel} envName={studyEnvParams.envName}/>
        <SurveyAutoCompleteButton surveyModel={surveyModel} envName={studyEnvParams.envName}/>
        {showHeaders && <h1 className="text-center mt-5 mb-0 pb-0 fw-bold">
          {i18n(`${form.stableId}:${form.version}`, { defaultValue: form.name })}
        </h1>}
        <SurveyComponent model={surveyModel}/>
        <SurveyFooter survey={form} surveyModel={surveyModel}/>
      </div>
    </>
  )
}
