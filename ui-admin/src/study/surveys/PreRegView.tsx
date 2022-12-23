import React, { useState } from 'react'
import { useParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyProvider'
import { useStudyEnvironmentOutlet } from 'study/StudyEnvironmentProvider'
import Api, { StudyEnvironment, Survey } from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string,
  version: string,
}

/** Preregistration editor.  This shares a LOT in common with SurveyView */
function RawPreRegView({ portalShortcode, currentEnv, survey, studyShortcode }:
                      {portalShortcode: string, currentEnv: StudyEnvironment, survey: Survey, studyShortcode: string}) {
  const [currentSurvey, setCurrentSurvey] = useState(survey)

  /** update the version */
  async function changeVersion(version: number): Promise<string> {
    throw `Not implemented ${version}`
  }


  /** saves as a new version and updates the study environment accordingly */
  async function createNewVersion(updatedContent: string): Promise<string> {
    survey.content = updatedContent
    try {
      const updatedSurvey = await Api.createNewSurveyVersion(portalShortcode, currentSurvey)
      setCurrentSurvey(updatedSurvey)
      const updatedEnv = { id: currentEnv.id, preRegSurveyId: updatedSurvey.id }
      const updatedStudyEnv = await Api.updateStudyEnvironment(portalShortcode, studyShortcode,
        currentEnv.environmentName, updatedEnv)
      currentEnv.preRegSurveyId = updatedStudyEnv.preRegSurveyId
      currentEnv.preRegSurvey = updatedSurvey
      Store.addNotification(successNotification(`Saved successfully`))
    } catch (e) {
      Store.addNotification(failureNotification(`Save failed`))
    }
    return updatedContent
  }

  return <SurveyEditorView portalShortcode={portalShortcode} currentForm={currentSurvey}
    createNewVersion={createNewVersion} changeVersion={changeVersion}/>
}

/** Routable component that delegates rendering to the raw view */
function PreRegView() {
  const params = useParams<SurveyParamsT>()
  const surveyStableId: string | undefined = params.surveyStableId

  const { portal, currentEnv, study } = useStudyEnvironmentOutlet()


  if (!surveyStableId) {
    return <span>you need to specify both name and version of the prereg urvey</span>
  }
  const survey = currentEnv.preRegSurvey
  if (survey.stableId != surveyStableId) {
    return <span>The survey {surveyStableId} does not exist on this study</span>
  }
  return <RawPreRegView portalShortcode={portal.shortcode} currentEnv={currentEnv}
    survey={survey} studyShortcode={study.shortcode}/>
}

export default PreRegView
