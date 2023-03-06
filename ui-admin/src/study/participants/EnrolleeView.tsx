import React from 'react'
import {
  ConsentResponse,
  Enrollee,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  SurveyResponse
} from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { NavLink, Route, Routes } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck'
import EnrolleeSurveyView from './survey/EnrolleeSurveyView'
import EnrolleeConsentView from './consent/EnrolleeConsentView'
import PreEnrollmentView from './survey/PreEnrollmentView'
import EnrolleeNotifications from './EnrolleeNotifications'
import DataChangeRecords from './DataChangeRecords'

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

/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export default function EnrolleeView({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT}) {
  const { currentEnv } = studyEnvContext

  /** gets classes to apply to nav links */
  function getLinkCssClasses({ isActive }: {isActive: boolean}) {
    return isActive ? 'fw-bold' : ''
  }

  const surveys = currentEnv.configuredSurveys
  const responseMap: ResponseMapT = {}
  surveys.forEach(configSurvey => {
    const matchedResponses = enrollee.surveyResponses.filter(response => configSurvey.survey.id === response.surveyId)
    responseMap[configSurvey.survey.stableId] = { survey: configSurvey, responses: matchedResponses }
  })

  const consents = currentEnv.configuredConsents
  const consentMap: ConsentResponseMapT = {}
  consents.forEach(configConsent => {
    const matchedResponses = enrollee.consentResponses
      .filter(response => configConsent.consentForm.id === response.consentFormId)
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
                <NavLink to="profile" className={getLinkCssClasses}>Profile</NavLink>
              </li>
              <li className="list-group-item">
                <NavLink to="preRegistration" className={getLinkCssClasses}>PreEnrollment</NavLink>
              </li>
              <li className="list-group-item subgroup">
                Consents
                <ul className="list-group">
                  { consents.map(consent => {
                    const stableId = consent.consentForm.stableId
                    return <li className="list-group-item border-0" key={stableId}>
                      <NavLink to={`consents/${stableId}`} className={getLinkCssClasses}>
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
                    return <li className="list-group-item border-0" key={stableId}>
                      <NavLink to={`surveys/${stableId}`} className={getLinkCssClasses}>
                        { survey.survey.name }
                        {responseMap[stableId].responses.length > 0 &&
                          <span className="badge align-middle" style={{  background: '#888', marginLeft: '0.5em' }}>
                            {responseMap[stableId].responses.length}
                          </span>
                        }
                      </NavLink>
                    </li>
                  }) }
                </ul>
              </li>
              <li className="list-group-item subgroup">
                Tasks
                <ul className="list-group">
                  { enrollee.participantTasks.map(task => {
                    return <li className="list-group-item border-0" key={task.id}>
                      <NavLink to={`tasks/${task.id}`} className={getLinkCssClasses}>
                        { task.taskType }: {task.targetName}
                        <span className="detail">{task.status}</span>
                      </NavLink>
                    </li>
                  }) }
                </ul>
              </li>
              <li className="list-group-item subgroup">
                <NavLink to="notifications" className={getLinkCssClasses}>Notifications</NavLink>
              </li>
              <li className="list-group-item subgroup">
                <NavLink to="changeRecords" className={getLinkCssClasses}>Audit history</NavLink>
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3">
            <Routes>
              <Route path="profile" element={<div>profile</div>}/>
              <Route path="consents" element={<div>consents</div>}/>
              <Route path="preRegistration" element={
                <PreEnrollmentView preEnrollSurvey={currentEnv.preEnrollSurvey}
                  preEnrollResponse={enrollee.preEnrollmentResponse}/>
              }/>
              <Route path="surveys">
                <Route path=":surveyStableId" element={<EnrolleeSurveyView enrollee={enrollee}
                  responseMap={responseMap}/>}/>
                <Route path="*" element={<div>Unknown participant survey page</div>}/>
              </Route>
              <Route path="consents">
                <Route path=":consentStableId" element={<EnrolleeConsentView enrollee={enrollee}
                  responseMap={consentMap}/>}/>
                <Route path="*" element={<div>Unknown participant survey page</div>}/>
              </Route>
              <Route path="notifications" element={
                <EnrolleeNotifications enrollee={enrollee} studyEnvContext={studyEnvContext}/>
              }/>
              <Route path="changeRecords" element={
                <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext}/>
              }/>
              <Route index element={<div>Enrollee page</div>}/>
              <Route path="*" element={<div>unknown enrollee route</div>}/>
            </Routes>
          </div>
        </div>
      </div>
    </div>
  </div>
}

/** TODO -- this should be computed server-side */
function isConsented(responses: ConsentResponse[]) {
  // for now, just check the most recent for consent
  return responses.length > 0 && responses[responses.length - 1].consented
}
