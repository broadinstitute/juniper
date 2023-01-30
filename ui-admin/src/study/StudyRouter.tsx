import React, { useContext } from 'react'
import { Link, Outlet, Route, Routes, useParams } from 'react-router-dom'
import { Study } from 'api/api'

import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import StudyEnvironmentRouter from './StudyEnvironmentRouter'
import StudyDashboard from './StudyDashboard'

export type StudyContextT = {
  updateStudy: (study: Study) => void
  study: Study
}

export type StudyParams = {
  studyShortcode: string,
  studyEnv: string,
}


/** puts a study in a context based on the url route */
export default function StudyRouter() {
  const params = useParams<StudyParams>()
  const studyShortname: string | undefined = params.studyShortcode

  if (!studyShortname) {
    return <span>No study selected</span>
  }
  return <StudyRouterFromShortcode shortcode={studyShortname}/>
}


/**
 * For now, this just reads the study from the existing PortalContext
 * eventually, we will want to load studies separately
 */
function StudyRouterFromShortcode({ shortcode }:
                       { shortcode: string}) {
  const portalState = useContext(PortalContext) as LoadedPortalContextT


  const matchedPortalStudy = portalState.portal.portalStudies.find(portalStudy => {
    return portalStudy.study.shortcode = shortcode
  })
  if (!matchedPortalStudy) {
    return <div>Study could not be loaded or found.</div>
  }

  const study = matchedPortalStudy?.study

  /** updates the study context -- does NOT update the server */
  function updateStudy(study: Study) {
    alert(`not implemented yet ${study.shortcode}`)
  }

  return <>
    <NavBreadcrumb>
      <Link className="text-white" to={`/${portalState.portal.shortcode}/studies/${study?.shortcode}`}>
        {study?.name}</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="env/:studyEnv/*" element={<StudyEnvironmentRouter study={study} updateStudy={updateStudy}/>}/>
      <Route index element={<StudyDashboard study={study} updateStudy={updateStudy}/>}/>
    </Routes>
  </>
}
