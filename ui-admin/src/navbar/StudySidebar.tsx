import { Portal, Study } from '@juniper/ui-core'
import { NavLink, useNavigate } from 'react-router-dom'
import {
  studyKitsPath,
  studyParticipantsPath
} from 'portal/PortalRouter'
import StudySelector from './StudySelector'
import React from 'react'
import {
  adminTasksPath,
  studyEnvDataBrowserPath, studyEnvDatasetListViewPath, studyEnvFormsPath,
  studyEnvMailingListPath, studyEnvMessagesPath,
  studyEnvMetricsPath, studyEnvNotificationsPath,
  studyEnvSiteContentPath, studyEnvSiteSettingsPath
} from '../study/StudyEnvironmentRouter'
import CollapsableMenu from './CollapsableMenu'
import { studyPublishingPath, studyUsersPath } from '../study/StudyRouter'

/** shows menu options related to the current study */
export const StudySidebar = ({ study, portalList, portalShortcode }:
                                 {study: Study, portalList: Portal[], portalShortcode: string}) => {
  const navigate = useNavigate()
  /** updates the selected study -- routes to that study's homepage */
  const setSelectedStudy = (portalShortcode: string, studyShortcode: string) => {
    navigate(studyParticipantsPath(portalShortcode, studyShortcode, 'live'))
  }
  const navStyleFunc = ({ isActive }: {isActive: boolean}) => {
    return isActive ? { background: '#567' } : {}
  }

  return <div className="pt-3">
    <StudySelector portalList={portalList} selectedShortcode={study.shortcode} setSelectedStudy={setSelectedStudy}/>
    <div className="text-white">
      <CollapsableMenu header={'Research Coordination'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyParticipantsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Participant List</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyKitsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Kits</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={adminTasksPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Tasks</NavLink>
        </li>
        <li>
          <NavLink to={studyEnvMailingListPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Mailing List</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Analytics & Data'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyEnvMetricsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Participant Analytics</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvDataBrowserPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Data Export</NavLink>
        </li>
        <li>
          <NavLink to={studyEnvDatasetListViewPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Terra Data Repo</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Design & Build'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyEnvSiteContentPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white p-1 rounded" style={navStyleFunc}>Website</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvFormsPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white p-1 rounded" style={navStyleFunc}>Forms &amp; Surveys</NavLink>
        </li>
        <li className="mb-2">
          <NavLink to={studyEnvNotificationsPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white p-1 rounded" style={navStyleFunc}>Emails &amp; Notifications</NavLink>
        </li>
        <li>
          <NavLink to={studyEnvMessagesPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white p-1 rounded" style={navStyleFunc}>Participant Dashboard</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Publish'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyPublishingPath(portalShortcode, study.shortcode)}
            className="text-white p-1 rounded" style={navStyleFunc}>Publish Content</NavLink>
        </li>
        <li>
          <NavLink to={studyEnvSiteSettingsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white p-1 rounded" style={navStyleFunc}>Site Settings</NavLink>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Administration'} content={<ul className="list-unstyled">
        <li className="mb-2">
          <NavLink to={studyUsersPath(portalShortcode, study.shortcode)}
            className="text-white p-1 rounded" style={navStyleFunc}>Manage Team</NavLink>
        </li>
      </ul>}/>

    </div>
  </div>
}
