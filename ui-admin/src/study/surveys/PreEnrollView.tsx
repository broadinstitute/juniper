import React, { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT, studyEnvFormsPath } from 'study/StudyEnvironmentRouter'
import Api, { Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import LoadingSpinner from 'util/LoadingSpinner'

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
    if (!survey || !currentSurvey) {
      return
    }

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
  const params = useParams<SurveyParamsT>()
  const surveyStableId: string | undefined = params.surveyStableId
  const [survey, setSurvey] = useState<Survey | undefined>()
  const [isLoading, setIsLoading] = useState(true)
  const { currentEnv } = studyEnvContext
  const [searchParams] = useSearchParams()
  const isReadOnly = searchParams.get('readOnly') === 'true'

  if (!surveyStableId) {
    return <span>you need to specify the stableId of the survey</span>
  }
  const envSurvey = currentEnv.preEnrollSurvey
  if (!envSurvey) {
    return <span>The survey {surveyStableId} does not exist in this environment</span>
  }
  /** load the survey from the server to get answer mappings and ensure we've got the latest content */
  useEffect(() => {
    setIsLoading(true)
    Api.getSurvey(studyEnvContext.portal.shortcode, surveyStableId, envSurvey.version).then(result => {
      setSurvey(result)
      setIsLoading(false)
    }).catch(e => {
      Store.addNotification(failureNotification(`could not load survey ${  e.message}`))
    })
  }, [studyEnvContext.portal.shortcode, studyEnvContext.currentEnv.environmentName, surveyStableId])

  return <>
    {isLoading && <LoadingSpinner/>}
    {!isLoading && <RawPreEnrollView studyEnvContext={studyEnvContext} survey={survey!} readOnly={isReadOnly}/>}
  </>
}

export default PreEnrollView
