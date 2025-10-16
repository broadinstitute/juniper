import React, { useState } from 'react'
import {
  ParticipantTask,
  StudyEnvironmentSurvey,
  SurveyResponse
} from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  Link,
  NavLink,
  Route,
  Routes
} from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import SurveyResponseView from '../survey/SurveyResponseView'
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
import {
  faCircleCheck,
  faCircleHalfStroke,
  faList,
  faMinus
} from '@fortawesome/free-solid-svg-icons'
import {
  faCircle as faEmptyCircle,
  faCircleXmark
} from '@fortawesome/free-regular-svg-icons'
import {
  Enrollee,
  ParticipantTaskStatus
} from '@juniper/ui-core'
import EnrolleeOverview from './EnrolleeOverview'
import {
  navDivStyle,
  navListItemStyle
} from 'util/subNavStyles'
import { RequireUserPermission } from 'util/RequireUserPermission'
import EnrolleeDocuments from './EnrolleeDocuments'
import { KitRequestFullDetails } from '../../kits/KitRequestFullDetails'


export type SurveyWithResponsesT = {
  survey: StudyEnvironmentSurvey
  responses: SurveyResponse[]
  tasks: ParticipantTask[]
}
export type ResponseMapT = { [stableId: string]: SurveyWithResponsesT }

/** loads an enrollee and renders the view for it */
export default function EnrolleeView({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { isLoading, enrollee, reload } = useRoutedEnrollee(studyEnvContext)

  return <>
    {isLoading && <LoadingSpinner/>}
    {!isLoading && enrollee &&
        <LoadedEnrolleeView
          enrollee={enrollee}
          studyEnvContext={studyEnvContext}
          onUpdate={reload}/>}
  </>
}

/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export function LoadedEnrolleeView({ enrollee, studyEnvContext, onUpdate }: {
  enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void
}) {
  const { currentEnv, currentEnvPath } = studyEnvContext
  const surveys: StudyEnvironmentSurvey[] = currentEnv.configuredSurveys

  /** generates a map of responses for the enrollee  we need to maintain this in state so that we can update the UX
   * without having to do full enrollee reloads when answers are submitted via the admin response editing UX */
  const generateResponseMap = () => {
    const updatedResponseMap: ResponseMapT = {}
    const sortedTasks = enrollee
      .participantTasks
      .sort((a, b) => b.createdAt! - a.createdAt!)
      .sort(
        (a, b) => (
          a.status === 'REMOVED' || a.status === 'REJECTED' ? 1 : 0
        ) - (
          b.status === 'REMOVED' || b.status === 'REJECTED' ? 1 : 0
        )
      ) // put removed/rejected tasks at the end

    surveys.forEach(configSurvey => {
      // to match responses to surveys, filter using the tasks, since those have the stableIds
      // this is valid since it's currently enforced that all survey responses are done as part of a task,
      const matchedTasks = sortedTasks
        .filter(task => task.targetStableId === configSurvey.survey.stableId)
      const matchedTaskResponseIds = matchedTasks.map(task => task.surveyResponseId)
      const matchedResponses = enrollee.surveyResponses
        .filter(response => matchedTaskResponseIds.includes(response.id))
        .sort((a, b) => b.createdAt! - a.createdAt!)
      updatedResponseMap[configSurvey.survey.stableId] = {
        survey: configSurvey,
        responses: matchedResponses,
        tasks: matchedTasks
      }
    })
    return updatedResponseMap
  }
  const [responseMap, setResponseMap] = useState<ResponseMapT>(generateResponseMap)

  const updateResponseMap = (stableId: string, response: SurveyResponse) => {
    const matchedResponseIndex = responseMap[stableId]?.responses
      .findIndex(r => r.id === response.id)
    const updatedResponses = responseMap[stableId] ? [...responseMap[stableId].responses] : []
    if (matchedResponseIndex !== -1) {
      updatedResponses[matchedResponseIndex] = response
    } else {
      updatedResponses.push(response)
    }
    setResponseMap({
      ...responseMap,
      [stableId]: {
        ...responseMap[stableId],
        responses: updatedResponses
      }
    })
  }

  const preEnrollSurvey = surveys
    .find(survey => survey.survey.surveyType === 'PRE_ENROLL')?.survey

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
                <NavLink end to="." className={getLinkCssClasses}>Overview</NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="profile" className={getLinkCssClasses}>Profile</NavLink>
              </li>
              {enrollee.subject && <>
                <li style={navListItemStyle}>
                  <CollapsableMenu header={'Forms'} headerClass="text-black" content={
                    <ul className="list-unstyled">
                      {preEnrollSurvey && <li className="mb-2">
                        <NavLink to="preRegistration" className={getLinkCssClasses}>
                        PreEnrollment
                        </NavLink>
                      </li>}
                      <SurveyList surveys={surveys
                        .filter(survey => survey.survey.surveyType === 'CONSENT')}
                      responseMap={responseMap} emptyText={'No consent forms'}/>
                    </ul>}/>
                </li>
                <li style={navListItemStyle}>
                  <CollapsableMenu header={'Research Surveys'} headerClass="text-black" content={
                    <SurveyList surveys={surveys
                      .filter(survey => survey.survey.surveyType === 'RESEARCH')}
                    responseMap={responseMap} emptyText={'No research forms'}/>}
                  />
                </li>
                <li style={navListItemStyle}>
                  <CollapsableMenu header={'Study Staff Forms'} headerClass="text-black" content={
                    <SurveyList surveys={surveys
                      .filter(survey => survey.survey.surveyType === 'ADMIN')}
                    responseMap={responseMap} emptyText={'No study staff forms'}/>}
                  />
                </li>
                <RequireUserPermission superuser>
                  <li style={navListItemStyle}>
                    <CollapsableMenu header={'Document Requests'} headerClass="text-black" content={
                      <>
                        <SurveyList surveys={surveys
                          .filter(survey => survey.survey.surveyType === 'DOCUMENT_REQUEST')}
                        responseMap={responseMap} emptyText={'No document requests'}
                        />
                        <NavLink to="documents" className={getLinkCssClasses}>
                          <FontAwesomeIcon className="me-2" icon={faList}/>
                          <span className={'fst-italic'}>View all documents</span>
                        </NavLink>
                      </>}
                    />
                  </li>
                </RequireUserPermission>
                <li style={navListItemStyle}>
                  <CollapsableMenu header={'Outreach'} headerClass="text-black" content={
                    <SurveyList surveys={surveys
                      .filter(survey => survey.survey.surveyType === 'OUTREACH')}
                    responseMap={responseMap} emptyText={'No outreach forms'}/>}
                  />
                </li>
                <li style={navListItemStyle} className="ps-3 d-flex justify-content-between align-items-center">
                  <NavLink to="kitRequests" className={getLinkCssClasses}>
                  Kit requests
                  </NavLink>
                  {
                    <span className="badge align-middle bg-secondary ms-1 mb-1">
                      {enrollee.kitRequests.length}
                    </span>
                  }
                </li>
              </>}
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
          <div className="participantTabContent flex-grow-1 bg-white p-3 pt-0">
            <ErrorBoundary>
              <Routes>
                <Route path="profile" element={<EnrolleeProfile enrollee={enrollee}
                  studyEnvContext={studyEnvContext}
                  onUpdate={onUpdate}/>}/>
                {preEnrollSurvey && <Route path="preRegistration/*" element={
                  <PreEnrollmentView preEnrollSurvey={preEnrollSurvey}
                    preEnrollResponse={enrollee.preEnrollmentResponse}
                    studyEnvContext={studyEnvContext}/>
                }/>}
                <Route path="surveys">
                  <Route path=":surveyStableId/*" element={<SurveyResponseView enrollee={enrollee}
                    responseMap={responseMap}
                    updateResponseMap={updateResponseMap}
                    studyEnvContext={studyEnvContext}
                    onUpdate={onUpdate}/>}/>
                  <Route path="*" element={<div>Unknown participant survey page</div>}/>
                </Route>
                <Route path="documents" element={
                  <EnrolleeDocuments enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route path="tasks" element={<ParticipantTaskView enrollee={enrollee}/>}/>
                <Route path="timeline" element={
                  <EnrolleeTimeline enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route path="changeRecords" element={
                  <DataChangeRecords enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route path="kitRequests" element={
                  <KitRequests enrollee={enrollee} studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
                }/>
                <Route path="kitRequests/:kitRequestId" element={
                  <KitRequestFullDetails enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route path="withdrawal" element={
                  <AdvancedOptions enrollee={enrollee} studyEnvContext={studyEnvContext}/>
                }/>
                <Route index element={<EnrolleeOverview enrollee={enrollee} studyEnvContext={studyEnvContext}
                  onUpdate={onUpdate}/>}/>
                <Route path="*" element={<div>The page you have navigated to does not exist.</div>}/>
              </Routes>
            </ErrorBoundary>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const SurveyList = ({ surveys, responseMap, emptyText }: {
  emptyText: string,
  surveys: StudyEnvironmentSurvey[], responseMap: ResponseMapT
}) => {
  return <ul className="list-unstyled">
    {surveys.length === 0 && <li className="mb-2">
      <span className="text-muted fst-italic">{emptyText}</span>
    </li>}
    {surveys.map(survey => {
      const stableId = survey.survey.stableId
      return <li className="mb-2 d-flex justify-content-between
                        align-items-center" key={stableId}>
        {createSurveyNavLink(stableId, responseMap, survey)}
        {badgeForResponses(responseMap[stableId]?.responses, responseMap[stableId]?.tasks)}
      </li>
    })}
  </ul>
}

/** returns an icon based on the enrollee's response to the most recent task */
const badgeForResponses = (responses: SurveyResponse[], tasks: ParticipantTask[]) => {
  if (!tasks?.length) {
    return statusDisplayMap['UNASSIGNED']
  }
  const nonRemovedTasks = tasks
    .filter(t => t.status !== 'REMOVED' && t.status !== 'REJECTED')
  if (!nonRemovedTasks.length) {
    return statusDisplayMap['REMOVED']
  }

  const lastTask = nonRemovedTasks[0]

  const numResponses = nonRemovedTasks.length
  return <div className="d-flex align-items-center justify-content-end">
    {statusDisplayMap[lastTask.status]}
    {numResponses > 1 && <span className="ms-1">({numResponses})</span>}
  </div>
}

/** gets classes to apply to nav links */
function getLinkCssClasses({ isActive }: { isActive: boolean }) {
  return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
}

export function surveyResponsePath(currentEnvPath: string, enrolleeShortcode: string,
  surveyStableId: string, taskId?: string) {
  return `${currentEnvPath}/participants/${enrolleeShortcode}/surveys/${surveyStableId}${taskId ?
    `?taskId=${taskId}` : ''}`
}
function createSurveyNavLink(stableId: string, responseMap: ResponseMapT, survey: StudyEnvironmentSurvey) {
  const taskId = responseMap[stableId]?.tasks[0]?.id
  const surveyPath = `surveys/${stableId}${taskId ? `?taskId=${taskId}` : ''}`

  return (
    <NavLink to={surveyPath} className={getLinkCssClasses}>
      {survey.survey.name}
    </NavLink>
  )
}

/** path to kit request list for enrollee */
export const enrolleeKitRequestPath = (currentEnvPath: string, enrolleeShortcode: string) => {
  return `${currentEnvPath}/participants/${enrolleeShortcode}/kitRequests`
}


export const statusDisplayMap: Record<ParticipantTaskStatus | 'UNASSIGNED', React.ReactNode> = {
  'COMPLETE': <FontAwesomeIcon icon={faCircleCheck} style={{ color: '#888' }} title="Complete"/>,
  'IN_PROGRESS': <FontAwesomeIcon icon={faCircleHalfStroke} style={{ color: '#888' }} title="In Progress"/>,
  'NEW': <FontAwesomeIcon icon={faEmptyCircle} style={{ color: '#888' }} title="No response"/>,
  'VIEWED': <FontAwesomeIcon icon={faEmptyCircle} style={{ color: '#888' }} title="Viewed"/>,
  'REJECTED': <FontAwesomeIcon icon={faCircleXmark} style={{ color: '#888' }} title="Rejected"/>,
  'REMOVED': <FontAwesomeIcon icon={faMinus} style={{ color: '#888' }} title="Removed"/>,
  'UNASSIGNED': <FontAwesomeIcon icon={faMinus} style={{ color: '#888' }} title="Not assigned"/>
}

