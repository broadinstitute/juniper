import React, { useState } from 'react'
import { Enrollee, StudyEnvironmentSurvey, Survey, SurveyResponse } from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyEditView from './SurveyEditView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { ResponseMapT } from '../EnrolleeView'
import { EnrolleeParams } from '../EnrolleeRouter'
import { instantToDefaultString } from '../../../util/timeUtils'


export default function EnrolleeSurveyView({ enrollee, responseMap }:
  {enrollee: Enrollee, responseMap: ResponseMapT}) {
  const params = useParams<EnrolleeParams>()

  const surveyStableId: string | undefined = params.surveyStableId

  if (!surveyStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[surveyStableId]
  if (!surveyAndResponses) {
    return <div>Unknown survey stableId</div>
  }

  return <RawEnrolleeSurveyView enrollee={enrollee}
    configSurvey={surveyAndResponses.survey} responses={surveyAndResponses.responses}/>
}

export function RawEnrolleeSurveyView({ enrollee, configSurvey, responses }:
  {enrollee: Enrollee, configSurvey: StudyEnvironmentSurvey, responses: SurveyResponse[]}) {
  const [isEditing, setIsEditing] = useState(false)
  if (responses.length === 0) {
    return <div>No responses</div>
  }
  // just show the last response for now
  const lastResponse = responses[responses.length - 1]
  const lastSnapshot = lastResponse.lastSnapshot
  if (!lastSnapshot) {
    return <div>Most recent response has no data yet </div>
  }

  return <div>
    <h6>{configSurvey.survey.name}</h6>
    <div>
      <span className="fst-italic">completed {instantToDefaultString(lastResponse.createdAt)}</span>
      <button className="ms-5 btn btn-secondary" onClick={() => setIsEditing(!isEditing)}>
        {isEditing ? 'cancel' : 'update / edit'}
      </button>
      <hr/>
      {!isEditing && <SurveyFullDataView fullData={lastSnapshot.fullData} survey={configSurvey.survey}/> }
      {isEditing && <SurveyEditView survey={configSurvey.survey} response={lastResponse}/>}
    </div>
  </div>
}
