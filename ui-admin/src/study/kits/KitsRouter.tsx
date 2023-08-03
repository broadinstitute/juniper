import React from 'react'
import { Link, Navigate, Route, Routes } from 'react-router-dom'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { studyKitsPath } from 'portal/PortalRouter'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import KitManager from './KitManager'

/** Router for kit management screens. */
export default function KitsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const {
    portal: { shortcode: portalShortcode },
    currentEnv: { environmentName },
    study: { shortcode: studyShortcode }
  } = studyEnvContext

  return <>
    <NavBreadcrumb>
      <Link to={studyKitsPath(portalShortcode, environmentName, studyShortcode)}>kits</Link>
    </NavBreadcrumb>
    <Routes>
      <Route index element={<Navigate to='byEnrollee' replace={true}/>}/>
      <Route path="byEnrollee" element={<KitManager studyEnvContext={studyEnvContext} tab='byEnrollee'/>}/>
      <Route path="byKit" element={<KitManager studyEnvContext={studyEnvContext} tab='byKit'/>}/>
    </Routes>
  </>
}
