import React from 'react'
import { Link, Navigate, NavLink, Route, Routes } from 'react-router-dom'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { studyKitsPath } from 'portal/PortalRouter'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import KitEnrolleeSelection from './KitEnrolleeSelection'
import KitList from './KitList'
import { renderPageHeader } from 'util/pageUtils'
import { tabLinkStyle } from '../../util/subNavStyles'

/** Router for kit management screens. */
export default function KitsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const {
    portal: { shortcode: portalShortcode },
    currentEnv: { environmentName },
    study: { shortcode: studyShortcode }
  } = studyEnvContext

  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      <Link to={studyKitsPath(portalShortcode, studyShortcode, environmentName)}>kits</Link>
    </NavBreadcrumb>
    <div className="container-fluid px-4 py-2">
      { renderPageHeader('Kits') }
      <div className="d-flex mb-2">
        <NavLink to="eligible" style={tabLinkStyle}>
          <div className="py-3 px-5">
            Eligible for kit
          </div>
        </NavLink>
        <NavLink to="requested" style={tabLinkStyle}>
          <div className="py-3 px-5">
            Requested
          </div>
        </NavLink>
      </div>
      <Routes>
        <Route index element={<Navigate to='eligible' replace={true}/>}/>
        <Route path="eligible" element={<KitEnrolleeSelection studyEnvContext={studyEnvContext}/>}/>
        <Route path="requested/*" element={<KitList studyEnvContext={studyEnvContext}/>}/>
      </Routes>
    </div>
  </>
}
