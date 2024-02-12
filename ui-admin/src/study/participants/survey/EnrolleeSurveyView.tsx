import React, { useState } from 'react'
import { Enrollee, StudyEnvironmentSurvey, SurveyResponse } from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyEditView from './SurveyEditView'
import { ResponseMapT } from '../enrolleeView/EnrolleeView'
import { EnrolleeParams } from '../enrolleeView/useRoutedEnrollee'
import { instantToDefaultString } from 'util/timeUtils'
import DocumentTitle from 'util/DocumentTitle'
import _uniq from 'lodash/uniq'
import pluralize from 'pluralize'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

/** Show responses for a survey based on url param */
export default function EnrolleeSurveyView({ enrollee, responseMap, studyEnvContext }:
  {enrollee: Enrollee, responseMap: ResponseMapT, studyEnvContext: StudyEnvContextT}) {
  const params = useParams<EnrolleeParams>()

  const surveyStableId: string | undefined = params.surveyStableId

  if (!surveyStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[surveyStableId]
  if (!surveyAndResponses) {
    return <div>Unknown survey stableId</div>
  }

  return <RawEnrolleeSurveyView enrollee={enrollee} studyEnvContext={studyEnvContext}
    configSurvey={surveyAndResponses.survey} responses={surveyAndResponses.responses}/>
}

/** show responses for a survey */
export function RawEnrolleeSurveyView({ enrollee, configSurvey, responses, studyEnvContext }: {
    enrollee: Enrollee, configSurvey: StudyEnvironmentSurvey,
  responses: SurveyResponse[], studyEnvContext: StudyEnvContextT
}) {
  const [isEditing, setIsEditing] = useState(false)
  if (responses.length === 0) {
    return <div>No responses for enrollee {enrollee.shortcode}</div>
  }
  // just show the last response for now
  const lastResponse = responses[responses.length - 1]
  if (!lastResponse.answers?.length) {
    return <div>Most recent response has no data yet </div>
  }

  const answerVersions = _uniq(lastResponse.answers.map(ans => ans.surveyVersion))
  const versionString = `${pluralize('version', answerVersions.length)} ${answerVersions.join(', ')}`

  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${configSurvey.survey.name}`}/>
    <h6>{configSurvey.survey.name}</h6>
    <div>
      <span className="fst-italic">
        <span>{lastResponse.complete ? 'Completed' : 'Last updated'}
          &nbsp; {instantToDefaultString(lastResponse.createdAt)}
        </span>
        &nbsp;
        <span>({versionString})</span>
      </span>

      <button className="ms-5 btn btn-secondary" onClick={() => setIsEditing(!isEditing)}>
        {isEditing ? 'cancel' : 'update / edit'}
      </button>
      <hr/>
      {!isEditing && <SurveyFullDataView answers={lastResponse.answers} survey={configSurvey.survey}
        userId={enrollee.participantUserId} studyEnvContext={studyEnvContext}/> }
      {isEditing && <SurveyEditView survey={configSurvey.survey} response={lastResponse}/>}
    </div>
  </div>
}
