import React from 'react'
import SurveyFullDataView from './SurveyFullDataView'
import { Answer, PreregistrationResponse, Survey } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'

/** show a preEnrollment response */
export default function PreEnrollmentView({ studyEnvContext, preEnrollResponse, preEnrollSurvey }: {
  studyEnvContext: StudyEnvContextT, preEnrollResponse?: PreregistrationResponse, preEnrollSurvey: Survey
}) {
  if (!preEnrollResponse) {
    return <span className="text-muted fst-italic"> no pre-enrollment data</span>
  }
  const answers: Answer[] = JSON.parse(preEnrollResponse.fullData)
  return <div>
    <h5>Pre-enrollment response</h5>
    {preEnrollResponse &&
        <SurveyFullDataView answers={answers} survey={preEnrollSurvey} studyEnvContext={studyEnvContext}/>
    }
  </div>
}
