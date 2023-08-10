import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT, studyEnvFormsPath } from 'study/StudyEnvironmentRouter'
import Api, { Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import LoadingSpinner from 'util/LoadingSpinner'
import { useLoadedSurvey, useSurveyParams } from './SurveyView'


export type SurveyParamsT = StudyParams & {
  surveyStableId: string,
  version: string,
}

/** Preregistration editor.  This shares a LOT in common with SurveyView, but it's expected they will diverge over
 * time as the mechanics for creating/editing pre-enroll surveys are likely to be different */
function RawPreEnrollView({ studyEnvContext, survey, readOnly = false }:
                              {studyEnvContext: StudyEnvContextT, survey: Survey, readOnly?: boolean}) {
  const { portal, study, currentEnv } = studyEnvContext
  const navigate = useNavigate()
  const [currentSurvey, setCurrentSurvey] = useState(survey)

  /** saves as a new version and updates the study environment accordingly */
  async function createNewVersion({ content: updatedContent }: { content: string }): Promise<void> {
    survey.content = updatedContent
    try {
      const updatedSurvey = await Api.createNewSurveyVersion(portal.shortcode, currentSurvey)
      setCurrentSurvey(updatedSurvey)
      const updatedEnv = { ...currentEnv, preEnrollSurveyId: updatedSurvey.id }
      const updatedStudyEnv = await Api.updateStudyEnvironment(portal.shortcode, study.shortcode,
        currentEnv.environmentName, updatedEnv)
      currentEnv.preEnrollSurveyId = updatedStudyEnv.preEnrollSurveyId
      currentEnv.preEnrollSurvey = updatedSurvey
      Store.addNotification(successNotification(`Saved successfully`))
    } catch (e) {
      Store.addNotification(failureNotification(`Save failed`))
    }
  }

  return <SurveyEditorView
    currentForm={currentSurvey}
    readOnly={readOnly}
    onCancel={() => navigate(studyEnvFormsPath(portal.shortcode, study.shortcode, currentEnv.environmentName))}
    onSave={createNewVersion}
  />
}

/** routable component for survey editing */
function PreEnrollView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { isReadOnly, version, stableId } = useSurveyParams()
  const { currentEnv, portal } = studyEnvContext

  const envSurvey = currentEnv.preEnrollSurvey
  const appliedVersion = version || envSurvey?.version
  if (!stableId) {
    return <span>You must specify a stableId for the survey to edit</span>
  }
  if (!appliedVersion) {
    return <span>The survey {stableId} is not already configured for this environment
      -- you must specify a version</span>
  }
  const { isLoading, survey } = useLoadedSurvey(portal.shortcode, stableId, appliedVersion)

  return <>
    { isLoading && <LoadingSpinner/> }
    { !isLoading && <RawPreEnrollView studyEnvContext={studyEnvContext} survey={survey!} readOnly={isReadOnly}/> }
  </>
}

export default PreEnrollView
