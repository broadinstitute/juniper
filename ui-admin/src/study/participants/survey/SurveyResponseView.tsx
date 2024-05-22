import React, { useState } from 'react'
import { StudyEnvironmentSurvey, SurveyResponse } from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyResponseEditor from './SurveyResponseEditor'
import { ResponseMapT } from '../enrolleeView/EnrolleeView'
import { EnrolleeParams } from '../enrolleeView/useRoutedEnrollee'
import { Enrollee, instantToDefaultString } from '@juniper/ui-core'
import DocumentTitle from 'util/DocumentTitle'
import _uniq from 'lodash/uniq'
import pluralize from 'pluralize'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { useUser } from 'user/UserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPencil, faX } from '@fortawesome/free-solid-svg-icons'

/** Show responses for a survey based on url param */
export default function SurveyResponseView({ enrollee, responseMap, studyEnvContext, onUpdate }:
  {enrollee: Enrollee, responseMap: ResponseMapT, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const params = useParams<EnrolleeParams>()

  const surveyStableId: string | undefined = params.surveyStableId

  if (!surveyStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[surveyStableId]
  if (!surveyAndResponses) {
    return <div>This survey has not been assigned to this participant</div>
  }
  // key forces the component to be destroyed/remounted when different survey selectect
  return <RawEnrolleeSurveyView key={surveyStableId} enrollee={enrollee} studyEnvContext={studyEnvContext}
    configSurvey={surveyAndResponses.survey} response={surveyAndResponses.response} onUpdate={onUpdate}/>
}

/** show responses for a survey */
export function RawEnrolleeSurveyView({ enrollee, configSurvey, response, studyEnvContext, onUpdate }: {
    enrollee: Enrollee, configSurvey: StudyEnvironmentSurvey,
  response?: SurveyResponse, studyEnvContext: StudyEnvContextT, onUpdate: () => void
}) {
  const [isEditing, setIsEditing] = useState(false)
  const { user } = useUser()
  // if this is a dedicated admin form, default to edit mode
  if (!configSurvey.survey.allowParticipantStart && configSurvey.survey.allowAdminEdit && user) {
    return <SurveyResponseEditor studyEnvContext={studyEnvContext} survey={configSurvey.survey}
      adminUserId={user.id} response={response} enrollee={enrollee} onUpdate={onUpdate}/>
  }

  if (!response && !configSurvey.survey.allowAdminEdit) {
    return <div>No response for enrollee {enrollee.shortcode}</div>
  }

  let versionString = ''
  if (response && response.answers.length) {
    const answerVersions = _uniq(response.answers.map(ans => ans.surveyVersion))
    versionString = `${pluralize('version', answerVersions.length)} ${answerVersions.join(', ')}`
  }

  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${configSurvey.survey.name}`}/>
    <h6>{configSurvey.survey.name}</h6>
    <div>
      <span className="fst-italic">
        {response && <><span>{response.complete ? 'Completed' : 'Last updated'}
          &nbsp; {instantToDefaultString(response.createdAt)}
        </span>
        &nbsp;
        <span>({versionString})</span></> }
      </span>

      { configSurvey.survey.allowAdminEdit && <button className="btn btn-secondary"
        onClick={() => {
          setIsEditing(!isEditing)
          if (isEditing) { onUpdate() }
        }
        }>
        {isEditing ?
          <div><FontAwesomeIcon icon={faX}/> Finish</div> :
          <div><FontAwesomeIcon icon={faPencil}/> Edit Response</div>
        }
      </button> }
      <hr/>
      {(!isEditing && !response?.answers.length) && <div>
        No response yet
      </div> }
      {(!isEditing && response?.answers.length) && <SurveyFullDataView answers={response?.answers || []}
        survey={configSurvey.survey}
        userId={enrollee.participantUserId} studyEnvContext={studyEnvContext}/> }
      {isEditing && user && <SurveyResponseEditor studyEnvContext={studyEnvContext}
        survey={configSurvey.survey} response={response} adminUserId={user.id}
        enrollee={enrollee} onUpdate={onUpdate}/>}
    </div>
  </div>
}