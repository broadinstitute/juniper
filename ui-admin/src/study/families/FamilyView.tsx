import React from 'react'
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
import ErrorBoundary from 'util/ErrorBoundary'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  faCircleCheck,
  faCircleHalfStroke
} from '@fortawesome/free-solid-svg-icons'
import {
  faCircle as faEmptyCircle,
  faCircleXmark
} from '@fortawesome/free-regular-svg-icons'
import {
  Family,
  ParticipantTaskStatus
} from '@juniper/ui-core'
import {
  navDivStyle,
  navListItemStyle
} from 'util/subNavStyles'
import useRoutedFamily from './useRoutedFamily'
import { FamilyOverview } from './FamilyOverview'


export type SurveyWithResponsesT = {
  survey: StudyEnvironmentSurvey
  response?: SurveyResponse
  task: ParticipantTask
}
export type ResponseMapT = { [stableId: string]: SurveyWithResponsesT }

/** loads an enrollee and renders the view for it */
export default function FamilyView({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) {
  const { isLoading, family, reload } = useRoutedFamily(studyEnvContext)
  return <>
    {isLoading && <LoadingSpinner/>}
    {!isLoading && family && <LoadedFamilyView family={family} studyEnvContext={studyEnvContext}
      onUpdate={reload}/>}
  </>
}

/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export function LoadedFamilyView({ family, studyEnvContext, onUpdate }:
                                     { family: Family, studyEnvContext: StudyEnvContextT, onUpdate: () => void }) {
  const { currentEnv, currentEnvPath } = studyEnvContext


  return <div className="ParticipantView mt-3 ps-4">
    <NavBreadcrumb value={family?.shortcode || ''}>
      <Link to={`${currentEnvPath}/family/${family.shortcode}`}>
        {family?.shortcode}</Link>
    </NavBreadcrumb>
    <div className="row">
      <div className="col-12">
        <h4>
          {family.shortcode} &nbsp;
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
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3 pt-0">
            <ErrorBoundary>
              <Routes>
                <Route index element={<FamilyOverview family={family} studyEnvContext={studyEnvContext}
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
const badgeForResponses = (response?: SurveyResponse) => {
  if (!response) {
    return statusDisplayMap['NEW']
  } else {
    if (response.complete) {
      return statusDisplayMap['COMPLETE']
    } else if (response.answers.length === 0) {
      return statusDisplayMap['VIEWED']
    } else {
      return statusDisplayMap['IN_PROGRESS']
    }
  }
}

/** gets classes to apply to nav links */
function getLinkCssClasses({ isActive }: { isActive: boolean }) {
  return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
}

function createSurveyNavLink(stableId: string, responseMap: ResponseMapT, survey: StudyEnvironmentSurvey) {
  const taskId = responseMap[stableId]?.task?.id
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


const statusDisplayMap: Record<ParticipantTaskStatus, React.ReactNode> = {
  'COMPLETE': <FontAwesomeIcon icon={faCircleCheck} style={{ color: '#888' }} title="Complete"/>,
  'IN_PROGRESS': <FontAwesomeIcon icon={faCircleHalfStroke} style={{ color: '#888' }} title="In Progress"/>,
  'NEW': <FontAwesomeIcon icon={faEmptyCircle} style={{ color: '#888' }} title="No response"/>,
  'VIEWED': <FontAwesomeIcon icon={faEmptyCircle} style={{ color: '#888' }} title="Viewed"/>,
  'REJECTED': <FontAwesomeIcon icon={faCircleXmark} style={{ color: '#888' }} title="Rejected"/>
}

