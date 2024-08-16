import React from 'react'
import {
  ParticipantTask,
  StudyEnvironmentSurvey,
  SurveyResponse
} from 'api/api'
import {
  familyPath,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
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
import { getFamilyNameString } from 'util/familyUtils'
import { useUser } from 'user/UserProvider'
import { FamilyAuditTable } from 'study/families/FamilyAuditTable'
import { FamilyActions } from 'study/families/FamilyActions'


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
      reloadFamily={reload}/>}
  </>
}

/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export function LoadedFamilyView({ family, studyEnvContext, reloadFamily }:
                                   { family: Family, studyEnvContext: StudyEnvContextT, reloadFamily: () => void }) {
  const { user } = useUser()


  return <div className="ParticipantView mt-3 ps-4">
    <NavBreadcrumb value={family?.shortcode || ''}>
      <Link to={`${familyPath(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName)}/${family.shortcode}`}>
        {family?.shortcode}</Link>
    </NavBreadcrumb>
    <div className="row">
      <div className="col-12">
        <h4>
          {getFamilyNameString(family)} Family&nbsp;
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
                <NavLink end to="." className={getLinkCssClasses}>Members & Relations</NavLink>
              </li>
              {user?.superuser && <li style={navListItemStyle} className="ps-3">
                <NavLink to="overview" className={getLinkCssClasses}>Overview<span
                  className='badge bg-primary fw-light ms-2'>BETA</span></NavLink>
              </li>}
              <li style={navListItemStyle} className="ps-3">
                <NavLink to="changeRecords" className={getLinkCssClasses}>Audit history</NavLink>
              </li>
              <li style={navListItemStyle} className="ps-3">
                <NavLink end to="actions" className={getLinkCssClasses}>Advanced actions</NavLink>
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3 pt-0">
            <ErrorBoundary>
              <Routes>
                <Route index
                  element={<FamilyMembersAndRelations family={family}
                    studyEnvContext={studyEnvContext}
                    reloadFamily={reloadFamily}/>}/>
                {/**
                 * eventually, we would want overview to be the index element
                 * so that there could be a readonly view for those without edit
                 * permissions
                 */}
                {user?.superuser && <Route path="overview" element={<FamilyOverview
                  family={family}
                  studyEnvContext={studyEnvContext}/>}
                />}
                <Route path="changeRecords" element={<FamilyAuditTable
                  family={family}
                  studyEnvContext={studyEnvContext}
                />}/>
                <Route path="actions" element={<FamilyActions
                  family={family}
                  studyEnvContext={studyEnvContext}
                />}/>
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


