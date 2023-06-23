import React, { useState } from 'react'
import { Enrollee, StudyEnvironmentSurvey, SurveyResponse } from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyEditView from './SurveyEditView'
import { ResponseMapT } from '../enrolleeView/EnrolleeView'
import { EnrolleeParams } from '../enrolleeView/EnrolleeLoader'
import { instantToDefaultString } from '../../../util/timeUtils'

/** Show responses for a survey based on url param */
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

/** show responses for a survey */
export function RawEnrolleeSurveyView({ enrollee, configSurvey, responses }:
  {enrollee: Enrollee, configSurvey: StudyEnvironmentSurvey, responses: SurveyResponse[]}) {
  const [isEditing, setIsEditing] = useState(false)
  if (responses.length === 0) {
    return <div>No responses for enrollee {enrollee.shortcode}</div>
  }
  // just show the last response for now
  const lastResponse = responses[responses.length - 1]
  if (!lastResponse.answers?.length) {
    return <div>Most recent response has no data yet </div>
  }

  return <div>
    <h6>{configSurvey.survey.name}</h6>
    <div>
      <span className="fst-italic">
        {lastResponse.complete ? 'Completed' : 'Last updated'} {instantToDefaultString(lastResponse.createdAt)}
      </span>
      <button className="ms-5 btn btn-secondary" onClick={() => setIsEditing(!isEditing)}>
        {isEditing ? 'cancel' : 'update / edit'}
      </button>
      <hr/>
      {!isEditing && <SurveyFullDataView answers={lastResponse.answers} survey={configSurvey.survey}/> }
      {isEditing && <SurveyEditView survey={configSurvey.survey} response={lastResponse}/>}
    </div>
  </div>
}
