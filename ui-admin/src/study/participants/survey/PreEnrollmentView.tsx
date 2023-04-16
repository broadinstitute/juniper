import React from 'react'
import SurveyFullDataView from './SurveyFullDataView'
import { Answer, PreregistrationResponse, Survey } from 'api/api'

/** show a preEnrollment response */
export default function PreEnrollmentView({ preEnrollResponse, preEnrollSurvey }:
{preEnrollResponse?: PreregistrationResponse, preEnrollSurvey: Survey}) {
  if (!preEnrollResponse) {
    return <span className="text-muted fst-italic"> no pre-enrollment data</span>
  }
  const answers: Answer[] = JSON.parse(preEnrollResponse.fullData)
  return <div>
    <h5>Pre-enrollment response</h5>
    {preEnrollResponse && <SurveyFullDataView answers={answers} survey={preEnrollSurvey}/>}
  </div>
}
