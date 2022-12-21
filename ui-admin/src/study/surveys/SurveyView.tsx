import React, { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyProvider'
import { useStudyEnvironmentOutlet } from 'study/StudyEnvironmentProvider'
import Api, { Portal, Study, StudyEnvironment, StudyEnvironmentSurvey, Survey } from 'api/api'
import VersionSelector from './VersionSelector'

import { SurveyCreatorComponent } from 'survey-creator-react'
import { failureNotification, successNotification } from 'util/notifications'
import { useSurveyJSCreator } from '../../util/surveyJSUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown } from '@fortawesome/free-solid-svg-icons/faCaretDown'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string
}

/** renders a survey for editing/viewing using the surveyJS editor */
function RawSurveyView({ portal, study, currentEnv, survey, readOnly = false }:
                      {portal: Portal, study: Study, currentEnv: StudyEnvironment,
                        survey: Survey, readOnly?: boolean}) {
  const navigate = useNavigate()
  const [isDirty, setIsDirty] = useState(false)
  const [currentSurvey, setCurrentSurvey] = useState(survey)
  const [showVersionSelector, setShowVersionSelector] = useState(false)

  const { surveyJSCreator } = useSurveyJSCreator(currentSurvey, handleSurveyModification)
  if (surveyJSCreator) {
    surveyJSCreator.readOnly = readOnly
  }
  /** indicate the survey has been modified */
  function handleSurveyModification() {
    if (!isDirty) {
      setIsDirty(true)
    }
  }

  /** saves the survey as a new version */
  async function publish() {
    if (!surveyJSCreator) {
      return
    }
    survey.content = surveyJSCreator.text
    try {
      const updatedSurvey = await Api.createNewVersion(portal.shortcode, currentSurvey)
      const configuredSurvey = currentEnv.configuredSurveys
        .find(s => s.survey.stableId === updatedSurvey.stableId) as StudyEnvironmentSurvey
      const updatedConfig = { ...configuredSurvey, surveyId: updatedSurvey.id }
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
    setIsDirty(false)
    updatedConfiguredSurvey.survey = updatedSurvey
    const configuredSurveyIndex = currentEnv.configuredSurveys
      .findIndex(s => s.survey.stableId === updatedSurvey.stableId)
    currentEnv.configuredSurveys[configuredSurveyIndex] = updatedConfiguredSurvey
    if (surveyJSCreator) {
      surveyJSCreator.text = updatedSurvey.content
    }
  }

  /** resets the survey to a previous version */
  async function restoreVersion(version: number) {
    setShowVersionSelector(false)
    try {
      // const updatedSurvey = await Api.updateConfiguredSurvey(portal.shortcode, currentEnv.environmentName,
      //   currentSurvey.stableId, version)
      // updateSurveyFromServer(updatedSurvey)
      // Store.addNotification(successNotification(`Set to version ${updatedSurvey.version}`))
    } catch (e) {
      Store.addNotification(failureNotification(`update failed`))
    }
  }

  /** handles the "cancel" button press */
  function handleCancel() {
    navigate('../../..')
  }

  return <div className="SurveyView">
    <div className="d-flex p-2 align-items-center">
      <div className="d-flex flex-grow-1">
        <h5>{currentSurvey.name}
          <span className="detail me-2 ms-2">version {currentSurvey.version}</span>
          { isDirty && <span className="badge" style={{ backgroundColor: 'rgb(51, 136, 0)' }} >
            <em>modified</em>
          </span> }
          <button className="btn-secondary btn" onClick={() => setShowVersionSelector(true)}>
            all versions <FontAwesomeIcon icon={faCaretDown}/>
          </button>
          { showVersionSelector && <VersionSelector studyShortname={portal.shortcode}
            stableId={currentSurvey.stableId}
            show={showVersionSelector} setShow={setShowVersionSelector}
            updateVersion={restoreVersion}/> }
        </h5>
      </div>
      {!readOnly && <button className="btn btn-primary me-md-2" type="button" onClick={publish}>
        Save
      </button> }
      <button className="btn btn-secondary" type="button" onClick={handleCancel}>Cancel</button>
    </div>
    {surveyJSCreator && <SurveyCreatorComponent creator={surveyJSCreator} /> }
  </div>
}

/** routable component for survey editing */
function SurveyView() {
  const params = useParams<SurveyParamsT>()
  const surveyStableId: string | undefined = params.surveyStableId

  const { portal, study, currentEnv } = useStudyEnvironmentOutlet()
  const [searchParams] = useSearchParams()
  const isReadonly = searchParams.get('mode') === 'view'

  if (!surveyStableId) {
    return <span>you need to specify the stableId of the survey</span>
  }

  const survey = currentEnv.configuredSurveys.find(s => s.survey.stableId === surveyStableId)?.survey
  if (!survey) {
    return <span>The survey {surveyStableId} does not exist in this environment</span>
  }
  return <RawSurveyView portal={portal} study={study} currentEnv={currentEnv} survey={survey} readOnly={isReadonly}/>
}

export default SurveyView
