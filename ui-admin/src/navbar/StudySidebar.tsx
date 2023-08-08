import { Portal, Study } from '@juniper/ui-core'
import { Link, useNavigate } from 'react-router-dom'
import {
  studyContentPath,
  studyKitsPath,
  studyParticipantsPath
} from 'portal/PortalRouter'
import StudySelector from './StudySelector'
import React from 'react'
import {
  studyEnvDataBrowserPath, studyEnvDatasetListViewPath,
  studyEnvMailingListPath,
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
  return <div className="pt-3">
    <StudySelector portalList={portalList} selectedShortcode={study.shortcode} setSelectedStudy={setSelectedStudy}/>
    <div className="text-white">
      <CollapsableMenu header={'Research Coordination'} content={<ul className="list-unstyled">
        <li className="mb-3">
          <Link to={studyParticipantsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Participant list</Link>
        </li>
        <li className="mb-3">
          <Link to={studyKitsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Biologistics</Link>
        </li>
        <li>
          <Link to={studyEnvMailingListPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Mailing list</Link>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Analytics & Data'} content={<ul className="list-unstyled">
        <li className="mb-3">
          <Link to={studyEnvMetricsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Participant Analytics</Link>
        </li>
        <li className="mb-3">
          <Link to={studyEnvDataBrowserPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Data Export</Link>
        </li>
        <li>
          <Link to={studyEnvDatasetListViewPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Terra Data Repo</Link>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Design & Build'} content={<ul className="list-unstyled">
        <li className="mb-3">
          <Link to={studyEnvSiteContentPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white">Website</Link>
        </li>
        <li className="mb-3">
          <Link to={studyContentPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white">Forms & Surveys</Link>
        </li>
        <li>
          <Link to={studyEnvNotificationsPath(portalShortcode, study.shortcode, 'sandbox')}
            className="text-white">Emails & Notifications</Link>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Publish'} content={<ul className="list-unstyled">
        <li className="mb-3">
          <Link to={studyPublishingPath(portalShortcode, study.shortcode)}
            className="text-white">Publish content</Link>
        </li>
        <li>
          <Link to={studyEnvSiteSettingsPath(portalShortcode, study.shortcode, 'live')}
            className="text-white">Site Settings</Link>
        </li>
      </ul>}/>
      <CollapsableMenu header={'Administration'} content={<ul className="list-unstyled">
        <li className="mb-3">
          <Link to={studyUsersPath(portalShortcode, study.shortcode)}
            className="text-white">Manage team</Link>
        </li>
      </ul>}/>

    </div>
  </div>
}
