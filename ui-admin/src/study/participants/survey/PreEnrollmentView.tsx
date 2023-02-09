import React from 'react'
import SurveyFullDataView from './SurveyFullDataView'
import { PreregistrationResponse, Survey } from 'api/api'


export default function PreEnrollmentView({ preEnrollResponse, preEnrollSurvey }:
{preEnrollResponse?: PreregistrationResponse, preEnrollSurvey: Survey}) {
  return <div>
    <h5>Pre-enrollment responses</h5>
    {!preEnrollResponse && <span className="detail"> no pre-enrollment data</span> }
    {preEnrollResponse && <SurveyFullDataView fullData={preEnrollResponse?.fullData} survey={preEnrollSurvey}/>}
  </div>
}
