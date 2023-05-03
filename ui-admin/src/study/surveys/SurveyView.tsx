import React, { useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, { Portal, Study, StudyEnvironment, StudyEnvironmentSurvey, Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string
}

/** Handles logic for updating study environment surveys */
function RawSurveyView({ portal, currentEnv, study, survey, readOnly = false }:
                      {portal: Portal, currentEnv: StudyEnvironment, study: Study,
                        survey: Survey, readOnly?: boolean}) {
  const [currentSurvey, setCurrentSurvey] = useState(survey)
  /** saves the survey as a new version */
  async function createNewVersion(updatedTextContent: string): Promise<string> {
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
      updatedTextContent = updatedSurvey.content
    } catch (e) {
      Store.addNotification(failureNotification(`save failed`))
    }
    return updatedTextContent
  }

  /** Syncs the survey with one from the server */
  function updateSurveyFromServer(updatedSurvey: Survey, updatedConfiguredSurvey: StudyEnvironmentSurvey) {
    setCurrentSurvey(updatedSurvey)
    updatedConfiguredSurvey.survey = updatedSurvey
    const configuredSurveyIndex = currentEnv.configuredSurveys
      .findIndex(s => s.survey.stableId === updatedSurvey.stableId)
    currentEnv.configuredSurveys[configuredSurveyIndex] = updatedConfiguredSurvey
  }

  /** resets the survey to a previous version */
  async function changeVersion(version: number) {
    // setShowVersionSelector(false)
    alert(`not implemented ${  version}`)
    try {
      // const updatedSurvey = await Api.updateConfiguredSurvey(portal.shortcode, currentEnv.environmentName,
      //   currentSurvey.stableId, version)
      // updateSurveyFromServer(updatedSurvey)
      // Store.addNotification(successNotification(`Set to version ${updatedSurvey.version}`))
    } catch (e) {
      Store.addNotification(failureNotification(`update failed`))
    }
  }

  return <SurveyEditorView portalShortcode={portal.shortcode} currentForm={currentSurvey} readOnly={readOnly}
    createNewVersion={createNewVersion} changeVersion={changeVersion}/>
}

/** routable component for survey editing */
function SurveyView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const params = useParams<SurveyParamsT>()
  const surveyStableId: string | undefined = params.surveyStableId

  const { portal, currentEnv, study } = studyEnvContext
  const [searchParams] = useSearchParams()
  const isReadOnly = searchParams.get('readOnly') === 'true'

  if (!surveyStableId) {
    return <span>you need to specify the stableId of the survey</span>
  }

  const survey = currentEnv.configuredSurveys.find(s => s.survey.stableId === surveyStableId)?.survey
  if (!survey) {
    return <span>The survey {surveyStableId} does not exist in this environment</span>
  }
  return <RawSurveyView portal={portal} study={study} currentEnv={currentEnv} survey={survey} readOnly={isReadOnly}/>
}

export default SurveyView
