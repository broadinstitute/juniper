import React  from 'react'
import {
  ConsentResponse, Enrollee,
  StudyEnvironmentConsent
} from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from 'study/participants/survey/SurveyFullDataView'
import { ConsentResponseMapT } from '../EnrolleeView'
import { EnrolleeParams } from '../EnrolleeRouter'
import { instantToDefaultString } from 'util/timeUtils'

/** shows consent forms for a given enrollee, based on url params specifying the form */
export default function EnrolleeConsentView({ enrollee, responseMap }:
                                             { enrollee: Enrollee, responseMap: ConsentResponseMapT}) {
  const params = useParams<EnrolleeParams>()

  const consentStableId: string | undefined = params.consentStableId

  if (!consentStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[consentStableId]
  if (!surveyAndResponses) {
    return <div>Unknown survey stableId</div>
  }

  return <RawEnrolleeConsentView enrollee={enrollee} configConsent={surveyAndResponses.consent}
    responses={surveyAndResponses.responses}/>
}

/** shows a given consent form for an enrollee */
export function RawEnrolleeConsentView({ enrollee, configConsent, responses }:
   {enrollee: Enrollee, configConsent: StudyEnvironmentConsent, responses: ConsentResponse[]}) {
  if (responses.length === 0) {
    return <div>No responses for {enrollee.shortcode}</div>
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
