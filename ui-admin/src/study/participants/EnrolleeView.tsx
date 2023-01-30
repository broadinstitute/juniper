import React from 'react'
import {
  ConsentForm,
  ConsentResponse,
  Enrollee,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse
} from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { NavLink, Outlet } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck'

export type SurveyWithResponsesT = {
  survey: StudyEnvironmentSurvey,
  responses: SurveyResponse[]
}
export type ResponseMapT = {[stableId: string] : SurveyWithResponsesT}


export type ConsentWithResponsesT = {
  consent: StudyEnvironmentConsent,
  responses: ConsentResponse[]
}
export type ConsentResponseMapT = {[stableId: string] : ConsentWithResponsesT}

export default function EnrolleeView({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT}) {
  const { currentEnv, study, portal } = studyEnvContext

  function getLinkStyle({ isActive }: {isActive: boolean}) {
    return `nav-link ${isActive ? 'active' : ''}`
  }

  const surveys = currentEnv.configuredSurveys
  const responseMap: ResponseMapT = {}
  surveys.forEach(configSurvey => {
    const matchedResponses = enrollee.surveyResponses.filter(response => configSurvey.id === response.surveyId)
    responseMap[configSurvey.survey.stableId] = { survey: configSurvey, responses: matchedResponses }
  })

  const consents = currentEnv.configuredConsents
  const consentMap: ConsentResponseMapT = {}
  consents.forEach(configConsent => {
    const matchedResponses = enrollee.consentResponses.filter(response => configConsent.id === response.consentFormId)
    consentMap[configConsent.consentForm.stableId] = { consent: configConsent, responses: matchedResponses }
  })

  return <div className="ParticipantView container mt-4">
    <div className="row">
      <div className="col-12">
        <h4>
          {enrollee.profile.givenName} {enrollee.profile.familyName} &nbsp;
          <span className="detail" title="Participant shortcode"> ({enrollee.shortcode})</span>
        </h4>
        <div>

        </div>
      </div>
    </div>
    <div className="row mt-2">
      <div className="col-12">
        <div className="d-flex">
          <div className="participantTabs">
            <ul className="list-group">
              <li className="list-group-item">
                <NavLink to="profile" className={getLinkStyle}>Profile</NavLink>
              </li>
              <li className="list-group-item">
                <NavLink to="preRegistration" className={getLinkStyle}>Preregistration</NavLink>
              </li>
              <li className="list-group-item subgroup">
                Consents
                <ul className="list-group">
                  { consents.map(consent => {
                    const stableId = consent.consentForm.stableId
                    return <li className="list-group-item" key={stableId}>
                      <NavLink to={`consents/${stableId}`} className={getLinkStyle}>
                        { consent.consentForm.name }
                        { isConsented(consentMap[stableId].responses) &&
                          <FontAwesomeIcon className="text-success ms-2 fa-lg" icon={faCheck}/>
                        }
                      </NavLink>
                    </li>
                  }) }
                </ul>
              </li>
              <li className="list-group-item subgroup">
                Surveys
                <ul className="list-group">
                  { surveys.map(survey => {
                    const stableId = survey.survey.stableId
                    return <li className="list-group-item" key={stableId}>
                      <NavLink to={`surveys/${stableId}`} className={getLinkStyle}>
                        { survey.survey.name }
                        {responseMap[stableId].responses.length > 0 &&
                          <span className="badge align-middle">{responseMap[stableId].responses.length}</span>
                        }
                      </NavLink>
                    </li>
                  }) }
                </ul>
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3">
            <Outlet/>
          </div>
        </div>
      </div>
    </div>
  </div>
}

function isConsented(responses: ConsentResponse[]) {
  // for now, just check the most recent for consent
  return responses.length > 0 && responses[responses.length - 1].consented
}
