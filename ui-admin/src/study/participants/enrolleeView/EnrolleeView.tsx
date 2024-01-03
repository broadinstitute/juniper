import React from 'react'
import { ConsentResponse, Enrollee, StudyEnvironmentConsent, StudyEnvironmentSurvey, SurveyResponse } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Link, NavLink, Route, Routes } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import EnrolleeSurveyView from '../survey/EnrolleeSurveyView'
import EnrolleeConsentView from '../consent/EnrolleeConsentView'
import PreEnrollmentView from '../survey/PreEnrollmentView'
import EnrolleeTimeline from './EnrolleeTimeline'
import DataChangeRecords from '../DataChangeRecords'
import EnrolleeProfile from './EnrolleeProfile'
import ParticipantTaskView from './ParticipantTaskView'
import ErrorBoundary from 'util/ErrorBoundary'
import AdvancedOptions from './AdvancedOptions'
import KitRequests from '../KitRequests'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import useRoutedEnrollee from './useRoutedEnrollee'
import LoadingSpinner from 'util/LoadingSpinner'
import CollapsableMenu from 'navbar/CollapsableMenu'
import { faCircleCheck, faCircleHalfStroke } from '@fortawesome/free-solid-svg-icons'
import { faCircle as faEmptyCircle, faCircleXmark } from '@fortawesome/free-regular-svg-icons'
import { ParticipantTaskStatus } from '@juniper/ui-core'
import EnrolleeOverview from './EnrolleeOverview'
import { navDivStyle, navListItemStyle } from 'util/subNavStyles'


export type SurveyWithResponsesT = {
  survey: StudyEnvironmentSurvey,
  responses: SurveyResponse[]
}
export type ResponseMapT = { [stableId: string]: SurveyWithResponsesT }


export type ConsentWithResponsesT = {
  consent: StudyEnvironmentConsent,
  responses: ConsentResponse[]
}
export type ConsentResponseMapT = { [stableId: string]: ConsentWithResponsesT }

/** loads an enrollee and renders the view for it */
export default function EnrolleeView({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { isLoading, enrollee, reload } = useRoutedEnrollee(studyEnvContext)
  return <>
    {isLoading && <LoadingSpinner/>}
    {!isLoading && enrollee && <LoadedEnrolleeView enrollee={enrollee} studyEnvContext={studyEnvContext}
      onUpdate={reload}/>}
  </>
}

/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export function LoadedEnrolleeView({ enrollee, studyEnvContext, onUpdate }:
                                     { enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void }) {
  const { currentEnv, currentEnvPath } = studyEnvContext

  /** gets classes to apply to nav links */
  function getLinkCssClasses({ isActive }: { isActive: boolean }) {
    return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
  }

  const surveys: StudyEnvironmentSurvey[] = currentEnv.configuredSurveys
  const responseMap: ResponseMapT = {}
  surveys.forEach(configSurvey => {
    // to match responses to surveys, filter using the tasks, since those have the stableIds
    // this is valid since it's currently enforced that all survey responses are done as part of a task,
    const matchedResponseIds = enrollee.participantTasks
      .filter(task => task.targetStableId === configSurvey.survey.stableId)
      .map(task => task.surveyResponseId)
    const matchedResponses = enrollee.surveyResponses
      .filter(response => matchedResponseIds.includes(response.id))
    responseMap[configSurvey.survey.stableId] = { survey: configSurvey, responses: matchedResponses }
  })

  const researchSurveys = surveys
    .filter(survey => survey.survey.surveyType === 'RESEARCH')
  const outreachSurveys = surveys
    .filter(survey => survey.survey.surveyType === 'OUTREACH')

  const consents = currentEnv.configuredConsents
  const consentMap: ConsentResponseMapT = {}
  consents.forEach(configConsent => {
    // to match responses to consents, filter using the tasks, since those have the stableIds
    // this is valid since it's currently enforced that all consents are done as part of a task,
    const matchedResponseIds = enrollee.participantTasks
      .filter(task => task.targetStableId === configConsent.consentForm.stableId)
      .map(task => task.consentResponseId)
    const matchedResponses = enrollee.consentResponses
      .filter(response => matchedResponseIds.includes(response.id))
    consentMap[configConsent.consentForm.stableId] = { consent: configConsent, responses: matchedResponses }
  })

  return <div className="ParticipantView mt-3 ps-4">
    <NavBreadcrumb value={enrollee?.shortcode || ''}>
      <Link to={`${currentEnvPath}/participants/${enrollee.shortcode}`}>
        {enrollee?.shortcode}</Link>
    </NavBreadcrumb>
    <div className="row">
      <div className="col-12">
        <h4>
          {enrollee.profile.givenName} {enrollee.profile.familyName} &nbsp;
          <span className="detail" title="Participant shortcode"> ({enrollee.shortcode})</span>
        </h4>
      </div>
    </div>
    <div className="row mt-2">
      <div className="col-12">
        <div className="d-flex">
          <div style={navDivStyle}>
            <ul className="list-unstyled">
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="." className={getLinkCssClasses}>Overview</NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="profile" className={getLinkCssClasses}>Profile</NavLink>
              </li>
              <li style={navListItemStyle}>
                <CollapsableMenu header={'Forms'} headerClass="text-black" content={
                  <ul className="list-unstyled">
                    {currentEnv.preEnrollSurvey && <li className="mb-2">
                      <NavLink to="preRegistration" className={getLinkCssClasses}>
                            PreEnrollment
                      </NavLink>
                    </li>}
                    {consents.map(consent => {
                      const stableId = consent.consentForm.stableId
                      return <li className="mb-2 d-flex justify-content-between align-items-center" key={stableId}>
                        <NavLink to={`consents/${stableId}`} className={getLinkCssClasses}>
                          {consent.consentForm.name}
                        </NavLink>
                        {isConsented(consentMap[stableId].responses) &&
                          statusDisplayMap['COMPLETE']
                        }
                      </li>
                    })}
                  </ul>}/>
              </li>
              <li style={navListItemStyle}>
                <CollapsableMenu header={'Surveys'} headerClass="text-black" content={
                  <ul className="list-unstyled">
                    {researchSurveys.map(survey => {
                      const stableId = survey.survey.stableId
                      return <li className="mb-2 d-flex justify-content-between
                        align-items-center" key={stableId}>
                        <NavLink to={`surveys/${stableId}`} className={getLinkCssClasses}>
                          {survey.survey.name}
                        </NavLink>
                        {badgeForResponses(responseMap[stableId].responses)}
                      </li>
                    })}
                  </ul>}
                />
              </li>
              <li style={navListItemStyle}>
                <CollapsableMenu header={'Outreach'} headerClass="text-black" content={
                  <ul className="list-unstyled">
                    {outreachSurveys.map(survey => {
                      const stableId = survey.survey.stableId
                      return <li className="mb-2 d-flex justify-content-between
                        align-items-center" key={stableId}>
                        <NavLink to={`surveys/${stableId}`} className={getLinkCssClasses}>
                          {survey.survey.name}
                        </NavLink>
                        {badgeForResponses(responseMap[stableId].responses)}
                      </li>
                    })}
                  </ul>}
                />
              </li>
              <li style={navListItemStyle} className="ps-3 d-flex justify-content-between align-items-center">
                <NavLink to="kitRequests" className={getLinkCssClasses}>
                  Kit requests
                </NavLink>
                {
                  enrollee.kitRequests.length > 0 &&
                    <span className="badge align-middle bg-secondary ms-1 mb-1">
                      {enrollee.kitRequests.length}
                    </span>
                }
              </li>
              <li style={navListItemStyle}>
                <CollapsableMenu header={'History & Advanced'} headerClass="text-black" content={
                  <ul className="list-unstyled">
                    <li className="mb-2">
                      <NavLink to="timeline" className={getLinkCssClasses}>Timeline</NavLink>
                    </li>
                    <li className="mb-2">
                      <NavLink to="tasks" className={getLinkCssClasses}>Task list</NavLink>
                    </li>
                    <li className="mb-2">
                      <NavLink to="changeRecords" className={getLinkCssClasses}>Audit history</NavLink>
                    </li>
                    <li className="mb-2">
                      <NavLink to="withdrawal" className={getLinkCssClasses}>Withdrawal</NavLink>
                    </li>
                  </ul>
                }/>
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3">
            <ErrorBoundary>
              <Routes>
                <Route path="profile" element={<EnrolleeProfile enrollee={enrollee}
                  studyEnvContext={studyEnvContext}
                  onUpdate={onUpdate}/>}/>
                {currentEnv.preEnrollSurvey && <Route path="preRegistration/*" element={
                  <PreEnrollmentView preEnrollSurvey={currentEnv.preEnrollSurvey}
                    preEnrollResponse={enrollee.preEnrollmentResponse}/>
                }/>}
                <Route path="surveys">
                  <Route path=":surveyStableId/*" element={<EnrolleeSurveyView enrollee={enrollee}
                    responseMap={responseMap}/>}/>
                  <Route path="*" element={<div>Unknown participant survey page</div>}/>
                </Route>
                <Route path="tasks" element={<ParticipantTaskView enrollee={enrollee}/>}/>
                <Route path="consents">
                  <Route path=":consentStableId/*" element={<EnrolleeConsentView enrollee={enrollee}
                    responseMap={consentMap}/>}/>
                  <Route path="*" element={<div>Unknown participant survey page</div>}/>
                </Route>
                <Route path="timeline" element={
                  <EnrolleeTimeline enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route path="changeRecords" element={
                  <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route path="kitRequests" element={
                  <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
                }/>
                <Route path="withdrawal" element={
                  <AdvancedOptions enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route index element={<EnrolleeOverview enrollee={enrollee} studyEnvContext={studyEnvContext}
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

/** returns an icon based on the enrollee's responses.  Note this does not handle multi-responses yet */
const badgeForResponses = (responses: SurveyResponse[]) => {
  if (responses.length === 0) {
    return statusDisplayMap['NEW']
  } else {
    if (responses[0].complete) {
      return statusDisplayMap['COMPLETE']
    } else if (responses[0].answers.length === 0) {
      return statusDisplayMap['VIEWED']
    } else {
      return statusDisplayMap['IN_PROGRESS']
    }
  }
}

/** path to kit request list for enrollee */
export const enrolleeKitRequestPath = (currentEnvPath: string, enrolleeShortcode: string) => {
  return `${currentEnvPath}/participants/${enrolleeShortcode}/kitRequests`
}

/** TODO -- this should be computed server-side */
function isConsented(responses: ConsentResponse[]) {
  // for now, just check the most recent for consent
  return responses.length > 0 && responses[responses.length - 1].consented
}

const statusDisplayMap: Record<ParticipantTaskStatus, React.ReactNode> = {
  'COMPLETE': <FontAwesomeIcon icon={faCircleCheck} style={{ color: '#888' }} title="Complete"/>,
  'IN_PROGRESS': <FontAwesomeIcon icon={faCircleHalfStroke} style={{ color: '#888' }} title="In Progress"/>,
  'NEW': <FontAwesomeIcon icon={faEmptyCircle} style={{ color: '#888' }} title="No response"/>,
  'VIEWED': <FontAwesomeIcon icon={faEmptyCircle} style={{ color: '#888' }} title="Viewed"/>,
  'REJECTED': <FontAwesomeIcon icon={faCircleXmark} style={{ color: '#888' }} title="Rejected"/>
}

