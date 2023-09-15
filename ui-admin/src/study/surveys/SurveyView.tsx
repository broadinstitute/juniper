import React, { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT, studyEnvFormsPath } from 'study/StudyEnvironmentRouter'
import Api, { StudyEnvironmentSurvey, Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import LoadingSpinner from 'util/LoadingSpinner'
import { useLoadingEffect } from '../../api/api-utils'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string,
  version?: string
}

/** Handles logic for updating study environment surveys */
function RawSurveyView({ studyEnvContext, survey, readOnly = false }:
                      {studyEnvContext: StudyEnvContextT, survey: Survey, readOnly?: boolean}) {
  const { portal, study, currentEnv } = studyEnvContext
  const navigate = useNavigate()

  const [currentSurvey, setCurrentSurvey] = useState(survey)
  /** saves the survey as a new version */
  async function createNewVersion({ content: updatedTextContent }: { content: string }): Promise<void> {
    try {
      const updatedSurvey = await Api.createNewSurveyVersion(
        portal.shortcode,
        { ...currentSurvey, content: updatedTextContent }
      )
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
      studyEnvContext={studyEnvContext}
      currentForm={currentSurvey}
      readOnly={readOnly}
      onCancel={() => navigate(studyEnvFormsPath(portal.shortcode, study.shortcode, currentEnv.environmentName))}
      onSave={createNewVersion}
    />
  )
}

/** loads a survey and associated data (e.g. answer mappings) */
export const useLoadedSurvey = (portalShortcode: string, stableId: string, version: number) => {
  const [survey, setSurvey] = useState<Survey | undefined>()

  /** load the survey from the server to get answer mappings and ensure we've got the latest content */
  const { isLoading } = useLoadingEffect(async () => {
    const survey = await Api.getSurvey(portalShortcode, stableId, version)
    setSurvey(survey)
  }, [portalShortcode, stableId, version])

  return { isLoading, survey }
}

/** read survey-related url params */
export const useSurveyParams = () => {
  const params = useParams<SurveyParamsT>()
  const [searchParams] = useSearchParams()
  const isReadOnly = searchParams.get('readOnly') === 'true'
  const version = params.version ? parseInt(params.version) : undefined

  return { isReadOnly, version, stableId: params.surveyStableId }
}

/** routable component for survey editing */
function SurveyView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { isReadOnly, version, stableId } = useSurveyParams()
  const { currentEnv, portal } = studyEnvContext
  const applyReadOnly = isReadOnly || currentEnv.environmentName !== 'sandbox'
  const envSurvey = currentEnv.configuredSurveys
    .find(s => s.survey.stableId === stableId)?.survey
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
    { !isLoading && survey && <RawSurveyView studyEnvContext={studyEnvContext}
      survey={survey} readOnly={applyReadOnly}/> }
  </>
}

export default SurveyView
