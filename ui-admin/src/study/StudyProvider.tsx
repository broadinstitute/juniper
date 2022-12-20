import React, { useContext } from 'react'
import { Link, Outlet, useParams } from 'react-router-dom'
import { Study } from 'api/api'

import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'
import { NavBreadcrumb } from 'navbar/AdminNavbar'

export type StudyContextT = {
  updateStudy: (study: Study) => void
  study: Study
}

export type StudyParams = {
  studyShortcode: string,
  studyEnv: string,
}

export const StudyContext = React.createContext<StudyContextT | null>(null)

/** puts a study in a context based on the url route */
export default function RoutableStudyProvider() {
  const params = useParams<StudyParams>()
  const studyShortname: string | undefined = params.studyShortcode

  if (!studyShortname) {
    return <span>No study selected</span>
  }
  return <StudyProvider shortcode={studyShortname}><Outlet/></StudyProvider>
}


/**
 * For now, this just reads the study from the existing PortalContext
 * eventually, we will want to load studies separately
 */
function StudyProvider({ shortcode, children }:
                       { shortcode: string, children: React.ReactNode}) {
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

  const studyContext = {
    study,
    updateStudy // eslint-disable-line no-unused-vars
  }

  return <StudyContext.Provider value={studyContext}>
    <NavBreadcrumb>
      <Link className="text-white" to={`/${portalState.portal.shortcode}/studies/${study?.shortcode}`}>
        {study?.name}</Link>
    </NavBreadcrumb>
    { children }
  </StudyContext.Provider>
}
