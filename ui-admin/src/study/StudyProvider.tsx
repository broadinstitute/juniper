import React, { useContext } from 'react'
import { Outlet, useParams } from 'react-router-dom'
import { Study } from 'api/api'

import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'

export type StudyContextT = {
  updateStudy: (study: Study) => void
  study: Study
}

export type StudyParams = {
  studyShortcode: string,
  studyEnv: string,
}

export const StudyContext = React.createContext<StudyContextT | null>(null)


export default function RoutableStudyProvider() {
  const params = useParams<StudyParams>()
  const studyShortname: string | undefined = params.studyShortcode
  const envName: string | undefined = params.studyEnv

  if (!studyShortname) {
    return <span>No study selected</span>
  }
  return <StudyProvider shortcode={studyShortname} envName={envName}><Outlet/></StudyProvider>
}


/**
 * For now, this just reads the study from the existing PortalContext
 * eventually, we will want to load studies separately
 */
function StudyProvider({ shortcode, envName, children }:
                       { shortcode: string, envName: string | undefined, children: any}) {
  const portalState = useContext(PortalContext) as LoadedPortalContextT
  const matchedPortalStudy = portalState.portal.portalStudies.find(portalStudy => {
    return portalStudy.study.shortcode = shortcode
  })

  if (!matchedPortalStudy) {
    return <div>Study &quot;{shortcode}&quot; could not be loaded or found.</div>
  }

  function updateStudy(study: Study) {
    alert('not implemented yet')
  }

  const studyContext = {
    study: matchedPortalStudy.study,
    updateStudy
  }

  return <StudyContext.Provider value={studyContext}>
    { children }
  </StudyContext.Provider>
}
