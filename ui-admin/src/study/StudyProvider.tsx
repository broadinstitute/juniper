import React, { useContext, useEffect } from 'react'
import { Link, Outlet, useParams } from 'react-router-dom'
import { Study } from 'api/api'

import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'
import { NavbarContext } from '../navbar/NavbarProvider'

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
  const navContext = useContext(NavbarContext)

  const matchedPortalStudy = portalState.portal.portalStudies.find(portalStudy => {
    return portalStudy.study.shortcode = shortcode
  })
  const study = matchedPortalStudy?.study
  useEffect(() => {
    const newCrumbs = [...navContext.breadCrumbs]
    newCrumbs[1] = <Link className="text-white" to={`/${portalState.portal.shortcode}/studies/${study?.shortcode}`}>
      {study?.name}</Link>
    navContext.setBreadCrumbs(newCrumbs)
  }, [])


  if (!matchedPortalStudy) {
    return <div>Study could not be loaded or found.</div>
  }
  /** updates the study context -- does NOT update the server */
  function updateStudy(study: Study) {
    alert(`not implemented yet ${study.shortcode}`)
  }

  const studyContext = {
    study: matchedPortalStudy.study,
    updateStudy // eslint-disable-line no-unused-vars
  }

  return <StudyContext.Provider value={studyContext}>
    { children }
  </StudyContext.Provider>
}
