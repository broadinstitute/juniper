import React  from 'react'
import {
  Answer, ConsentForm,
  ConsentResponse, Enrollee,
  StudyEnvironmentConsent
} from 'api/api'

import { useParams } from 'react-router-dom'
import SurveyFullDataView from 'study/participants/survey/SurveyFullDataView'
import { ConsentResponseMapT } from '../enrolleeView/EnrolleeView'
import { EnrolleeParams } from '../enrolleeView/useRoutedEnrollee'
import { instantToDefaultString } from '@juniper/ui-core'
import DocumentTitle from 'util/DocumentTitle'

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
  const answers = extractAnswers(lastResponse, configConsent.consentForm)
  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${configConsent.consentForm.name}`}/>
    <h6>{configConsent.consentForm.name}</h6>
    <div>
      <span className="fst-italic">completed {instantToDefaultString(lastResponse.createdAt)}</span>
      <hr/>
      <SurveyFullDataView answers={answers} survey={configConsent.consentForm}/>
    </div>
  </div>
}

/**
 * responses to ConsentForms are stored as single documents, rather than Answers, since they are not permitted
 * to be filled out across multiple versions.
 * So to use SurveyFullDataView to show the responses, first convert it to a list of answers
 */
const extractAnswers = (response: ConsentResponse, consentForm: ConsentForm) => {
  const answers: Answer[] = JSON.parse(response.fullData)
  answers.forEach(answer => {
    answer.surveyVersion = consentForm.version
  })
  return answers
}
