import React, { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT, studyEnvFormsPath } from 'study/StudyEnvironmentRouter'
import Api, { StudyEnvironmentSurvey, Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import { useUser } from 'user/UserProvider'
import LoadingSpinner from '../../util/LoadingSpinner'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string
}

/** Handles logic for updating study environment surveys */
function RawSurveyView({ studyEnvContext, survey, readOnly = false }:
                      {studyEnvContext: StudyEnvContextT, survey: Survey, readOnly?: boolean}) {
  const { portal, study, currentEnv } = studyEnvContext
  const navigate = useNavigate()
  const { user } = useUser()

  const [currentSurvey, setCurrentSurvey] = useState(survey)
  /** saves the survey as a new version */
  async function createNewVersion({ content: updatedTextContent }: { content: string }): Promise<void> {
    if (!user.superuser) {
      Store.addNotification(failureNotification('you do not have permissions to save surveys'))
      return
    }

    survey.content = updatedTextContent
    try {
      const updatedSurvey = await Api.createNewSurveyVersion(portal.shortcode, currentSurvey)
      const configuredSurvey = currentEnv.configuredSurveys
        .find(s => s.survey.stableId === updatedSurvey.stableId) as StudyEnvironmentSurvey
      const updatedConfig = { ...configuredSurvey, surveyId: updatedSurvey.id, survey: updatedSurvey }
      const updatedConfiguredSurvey = await Api.updateConfiguredSurvey(portal.shortcode,
        study.shortcode, currentEnv.environmentName, updatedConfig)
      Store.addNotification(successNotification(
        `Updated ${currentEnv.environmentName} to version ${updatedSurvey.version}`
      ))
      updateSurveyFromServer(updatedSurvey, updatedConfiguredSurvey)
    } catch (e) {
      Store.addNotification(failureNotification(`save failed`))
    }
  }

  /** Syncs the survey with one from the server */
  function updateSurveyFromServer(updatedSurvey: Survey, updatedConfiguredSurvey: StudyEnvironmentSurvey) {
    setCurrentSurvey(updatedSurvey)
    updatedConfiguredSurvey.survey = updatedSurvey
    const configuredSurveyIndex = currentEnv.configuredSurveys
      .findIndex(s => s.survey.stableId === updatedSurvey.stableId)
    currentEnv.configuredSurveys[configuredSurveyIndex] = updatedConfiguredSurvey
  }

  return (
    <SurveyEditorView
      currentForm={currentSurvey}
      readOnly={readOnly}
      onCancel={() => navigate(studyEnvFormsPath(portal.shortcode, study.shortcode, currentEnv.environmentName))}
      onSave={createNewVersion}
    />
  )
}

/** routable component for survey editing */
function SurveyView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
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
  const envSurvey = currentEnv.configuredSurveys
    .find(s => s.survey.stableId === surveyStableId)?.survey
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
    { isLoading && <LoadingSpinner/> }
    { !isLoading && <RawSurveyView studyEnvContext={studyEnvContext} survey={survey!} readOnly={isReadOnly}/> }
  </>
}

export default SurveyView
