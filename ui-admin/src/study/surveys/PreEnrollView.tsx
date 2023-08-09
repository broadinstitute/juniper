import React, { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import { useUser } from 'user/UserProvider'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string,
  version: string,
}

/** Preregistration editor.  This shares a LOT in common with SurveyView */
function RawPreRegView({ studyEnvContext, survey, readOnly }:
                      { studyEnvContext: StudyEnvContextT, readOnly: boolean
                        survey: Survey}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const { user } = useUser()
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

  return (
    <SurveyEditorView
      currentForm={currentSurvey}
      readOnly={readOnly}
      onCancel={() => navigate(currentEnvPath)}
      onSave={createNewVersion}
    />
  )
}

/** Routable component that delegates rendering to the raw view */
function PreEnrollView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const params = useParams<SurveyParamsT>()
  const surveyStableId: string | undefined = params.surveyStableId

  const { currentEnv } = studyEnvContext
  const [searchParams] = useSearchParams()
  const readOnly = searchParams.get('readOnly') === 'true'

  if (!surveyStableId) {
    return <span>you need to specify both name and version of the prereg urvey</span>
  }
  const survey = currentEnv.preEnrollSurvey
  if (survey?.stableId != surveyStableId) {
    return <span>The survey {surveyStableId} does not exist on this study</span>
  }
  return <RawPreRegView studyEnvContext={studyEnvContext} survey={survey} readOnly={readOnly}/>
}

export default PreEnrollView
