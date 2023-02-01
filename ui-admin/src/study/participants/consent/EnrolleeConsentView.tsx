import React, { useState } from 'react'
import {
  ConsentResponse,
  Enrollee,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse
} from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from 'study/participants/survey/SurveyFullDataView'
import SurveyEditView from 'study/participants/survey/SurveyEditView'
import { ConsentResponseMapT, ResponseMapT } from '../EnrolleeView'
import { EnrolleeParams } from '../EnrolleeRouter'
import { instantToDefaultString } from '../../../util/timeUtils'


export default function EnrolleeConsentView({ enrollee, responseMap }:
                                             {enrollee: Enrollee, responseMap: ConsentResponseMapT}) {
  const params = useParams<EnrolleeParams>()

  const consentStableId: string | undefined = params.consentStableId

  if (!consentStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[consentStableId]
  if (!surveyAndResponses) {
    return <div>Unknown survey stableId</div>
  }

  return <RawEnrolleeConsentView enrollee={enrollee}
    configConsent={surveyAndResponses.consent} responses={surveyAndResponses.responses}/>
}

export function RawEnrolleeConsentView({ enrollee, configConsent, responses }:
   {enrollee: Enrollee, configConsent: StudyEnvironmentConsent, responses: ConsentResponse[]}) {
  const [isEditing, setIsEditing] = useState(false)
  if (responses.length === 0) {
    return <div>No responses</div>
  }
  // just show the last response for now
  const lastResponse = responses[responses.length - 1]

  return <div>
    <h6>{configConsent.consentForm.name}</h6>
    <div>
      <span className="fst-italic">completed {instantToDefaultString(lastResponse.createdAt)}</span>
      <hr/>
      <SurveyFullDataView fullData={lastResponse.fullData} survey={configConsent.consentForm}/>
    </div>
  </div>
}
