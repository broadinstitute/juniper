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
import ErrorBoundary from 'util/ErrorBoundary'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import LoadingSpinner from 'util/LoadingSpinner'

import { Family } from '@juniper/ui-core'
import {
  navDivStyle,
  navListItemStyle
} from 'util/subNavStyles'
import useRoutedFamily from './useRoutedFamily'
import { FamilyOverview } from './FamilyOverview'
import { FamilyMembersAndRelations } from 'study/families/FamilyMembersAndRelations'
import { getFamilyNames } from 'util/familyUtils'


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
export function LoadedFamilyView({ family, studyEnvContext }:
                                     { family: Family, studyEnvContext: StudyEnvContextT, onUpdate: () => void }) {
  const { currentEnvPath } = studyEnvContext


  return <div className="ParticipantView mt-3 ps-4">
    <NavBreadcrumb value={family?.shortcode || ''}>
      <Link to={`${currentEnvPath}/family/${family.shortcode}`}>
        {family?.shortcode}</Link>
    </NavBreadcrumb>
    <div className="row">
      <div className="col-12">
        <h4>
          {getFamilyNames(family)} Family&nbsp;
          <span className="detail" title="Participant shortcode"> ({family.shortcode})</span>
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
                <NavLink to="membersAndRelations" className={getLinkCssClasses}>Members & Relations</NavLink>
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3 pt-0">
            <ErrorBoundary>
              <Routes>
                <Route index element={<FamilyOverview family={family} studyEnvContext={studyEnvContext}/>}/>
                <Route path="membersAndRelations"
                  element={<FamilyMembersAndRelations family={family} studyEnvContext={studyEnvContext}/>}/>
                <Route path="*" element={<div>unknown enrollee route</div>}/>
              </Routes>
            </ErrorBoundary>
          </div>
        </div>
      </div>
    </div>
  </div>
}

/** gets classes to apply to nav links */
function getLinkCssClasses({ isActive }: { isActive: boolean }) {
  return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
}


