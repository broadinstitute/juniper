import React from 'react'
import {
  ConsentResponse,
  Enrollee, ParticipantTask,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  SurveyResponse
} from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { NavLink, Route, Routes } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck'
import EnrolleeSurveyView from '../survey/EnrolleeSurveyView'
import EnrolleeConsentView from '../consent/EnrolleeConsentView'
import PreEnrollmentView from '../survey/PreEnrollmentView'
import EnrolleeNotifications from './EnrolleeNotifications'
import DataChangeRecords from '../DataChangeRecords'
import EnrolleeProfile from './EnrolleeProfile'
import ParticipantTaskView from './ParticipantTaskView'
import ErrorBoundary from 'util/ErrorBoundary'
import AdvancedOptions from './AdvancedOptions'
import KitRequests from '../KitRequests'

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
export default function EnrolleeView({ enrollee, studyEnvContext, onUpdate }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const { currentEnv, currentEnvPath } = studyEnvContext

  /** gets classes to apply to nav links */
  function getLinkCssClasses({ isActive }: {isActive: boolean}) {
    return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
  }

  const surveys = currentEnv.configuredSurveys
  const responseMap: ResponseMapT = {}
  surveys.forEach(configSurvey => {
    // to match responses to surveys, filter using the tasks, since those have the stableIds
    // this is valid since it's currently enforced that all survey responses are done as part of a task,
    const matchedResponseIds = enrollee.participantTasks
      .filter(task => task.targetStableId === configSurvey.survey.stableId)
      .map(task => task.surveyResponseId)
    const matchedResponses = enrollee.surveyResponses
      .filter(response => matchedResponseIds.includes(response.id as string))
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
                <NavLink to="profile" className={getLinkCssClasses}>Profile &amp; Notes</NavLink>
              </li>
              <li className="list-group-item subgroup">
                <NavLink to="tasks" className={getLinkCssClasses}>Tasks</NavLink>
                <TaskSummary tasks={enrollee.participantTasks}/>
              </li>
              <li className="list-group-item">
                { currentEnv.preEnrollSurvey && <NavLink to="preRegistration" className={getLinkCssClasses}>
                  PreEnrollment
                </NavLink> }
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
                          <span className="badge align-middle bg-secondary ms-1 mb-1">
                            {responseMap[stableId].responses.length}
                          </span>
                        }
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
              <li className="list-group-item subgroup">
                <NavLink to="kitRequests" className={getLinkCssClasses}>
                  Kit requests
                  {
                    enrollee.kitRequests.length > 0 &&
                      <span className="badge align-middle bg-secondary ms-1 mb-1">
                        {enrollee.kitRequests.length}
                      </span>
                  }
                </NavLink>
              </li>
              <li className="list-group-item subgroup">
                <NavLink to="advanced" className={getLinkCssClasses}>Advanced options</NavLink>
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3">
            <ErrorBoundary>
              <Routes>
                <Route path="profile" element={<EnrolleeProfile enrollee={enrollee}
                                                                studyEnvContext={studyEnvContext}
                                                                onUpdate={onUpdate}/>}/>
                <Route path="consents" element={<div>consents</div>}/>
                { currentEnv.preEnrollSurvey && <Route path="preRegistration" element={
                  <PreEnrollmentView preEnrollSurvey={currentEnv.preEnrollSurvey}
                    preEnrollResponse={enrollee.preEnrollmentResponse}/>
                }/> }
                <Route path="surveys">
                  <Route path=":surveyStableId" element={<EnrolleeSurveyView enrollee={enrollee}
                    responseMap={responseMap}/>}/>
                  <Route path="*" element={<div>Unknown participant survey page</div>}/>
                </Route>
                <Route path="tasks" element={<ParticipantTaskView enrollee={enrollee}/>}/>
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
                <Route path="kitRequests" element={
                  <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
                }/>
                <Route path="advanced" element={
                  <AdvancedOptions enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route index element={<EnrolleeProfile enrollee={enrollee} studyEnvContext={studyEnvContext}
                                                       onUpdate={onUpdate}/>}/>
                <Route path="*" element={<div>unknown enrollee route</div>}/>
              </Routes>
            </ErrorBoundary>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const TaskSummary = ({ tasks }: {tasks: ParticipantTask[]}) => {
  const countsWithLabels: {label: string, count :number}[] = [
    { label: 'New', count: tasks.filter(task => task.status === 'NEW').length },
    { label: 'In progress', count: tasks.filter(task => task.status === 'IN_PROGRESS').length },
    { label: 'Complete', count: tasks.filter(task => task.status === 'COMPLETE').length }

  ]
  return <ul className="list-unstyled">
    {countsWithLabels.map(countWithLabel => <li key={countWithLabel.label} className="ms-3 d-flex align-items-center">
      {countWithLabel.label}: <span className="badge align-middle bg-secondary ms-1">
        {countWithLabel.count}
      </span>
    </li>)}
  </ul>
}

export const enrolleeKitRequestPath = (currentEnvPath: string, enrolleeShortcode: string) => {
  return `${currentEnvPath}/participants/${enrolleeShortcode}/kitRequests`
}

/** TODO -- this should be computed server-side */
function isConsented(responses: ConsentResponse[]) {
  // for now, just check the most recent for consent
  return responses.length > 0 && responses[responses.length - 1].consented
}
