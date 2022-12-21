import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyProvider'
import { useStudyEnvironmentOutlet } from 'study/StudyEnvironmentProvider'
import Api, { Portal, StudyEnvironment, Survey } from 'api/api'

import { SurveyCreator, SurveyCreatorComponent } from 'survey-creator-react'
import { failureNotification, successNotification } from 'util/notifications'

export type SurveyParamsT = StudyParams & {
  surveyStableId: string,
  version: string,
}

/** Preregistration editor.  This shares a LOT in common with SurveyView */
function RawPreRegView({ portal, currentEnv, survey }:
                      {portal: Portal, currentEnv: StudyEnvironment, survey: Survey}) {
  const navigate = useNavigate()
  const [isDirty, setIsDirty] = useState(false)
  const [currentSurvey, setCurrentSurvey] = useState(survey)
  const [surveyJSCreator, setSurveyJSCreator] = useState<SurveyCreator | null>(null)

  /** sets the dirty flag on modifications */
  function handleSurveyModification() {
    if (!isDirty) {
      setIsDirty(true)
    }
  }

  useEffect(() => {
    const creatorOptions = {
      showLogicTab: false,
      isAutoSave: false,
      showSurveyTitle: false
    }
    const newSurveyJSCreator = new SurveyCreator(creatorOptions)
    newSurveyJSCreator.text = currentSurvey.content
    newSurveyJSCreator.survey.title = currentSurvey.name
    newSurveyJSCreator.onModified.add(handleSurveyModification)
    setSurveyJSCreator(newSurveyJSCreator)
  }, [])

  /** saves as a new version and updates the study environment accordingly */
  async function publish() {
    if (!surveyJSCreator) {
      return
    }
    survey.content = surveyJSCreator.text
    try {
      const updatedSurvey = await Api.createNewPreRegVersion(portal.shortcode, currentSurvey)
      setCurrentSurvey(updatedSurvey)
      currentEnv.preRegSurvey = updatedSurvey
      Store.addNotification(successNotification(`Saved successfully`))
    } catch (e) {
      Store.addNotification(failureNotification(`save failed`))
    }
  }

  /** goes back to the envrironment view */
  function handleCancel() {
    navigate('../../..')
  }

  return <div className="PreRegView">
    <div className="d-flex p-2 align-items-center">
      <div className="d-flex flex-grow-1">
        <h5>{currentSurvey.name}
          <span className="detail me-2 ms-2">version {currentSurvey.version}</span>
          { isDirty && <span className="badge " style={{ backgroundColor: 'rgb(51, 136, 0)' }}>
            <em>modified</em>
          </span> }
        </h5>
      </div>
      <button className="btn btn-primary me-md-2" type="button" onClick={publish}>
        Save
      </button>
      <button className="btn btn-secondary" type="button" onClick={handleCancel}>Cancel</button>
    </div>
    {surveyJSCreator && <SurveyCreatorComponent creator={surveyJSCreator}/>}
  </div>
}

/** Routable component that delegates rendering to the raw view */
function PreRegView() {
  const params = useParams<SurveyParamsT>()
  const surveyStableId: string | undefined = params.surveyStableId

  const { portal, currentEnv } = useStudyEnvironmentOutlet()


  if (!surveyStableId) {
    return <span>you need to specify both name and version of the prereg urvey</span>
  }
  const survey = currentEnv.preRegSurvey
  if (survey.stableId != surveyStableId) {
    return <span>The survey {surveyStableId} does not exist on this study</span>
  }
  return <RawPreRegView portal={portal} currentEnv={currentEnv} survey={survey}/>
}

export default PreRegView
