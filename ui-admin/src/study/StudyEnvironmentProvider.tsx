import React, { useEffect, useContext } from 'react'
import { Portal, Study, StudyEnvironment } from 'api/api'
import { StudyParams, StudyContext } from 'study/StudyProvider'

import { Link, Outlet, useOutletContext, useParams } from 'react-router-dom'
import { NavBreadcrumb } from '../navbar/AdminNavbar'
import { NavbarContext } from '../navbar/NavbarProvider'
import StudyEnvironmentSidebar from './StudyEnvironmentSidebar'
import { PortalContext } from '../portal/PortalProvider'


export type StudyContextT = { study: Study, currentEnv: StudyEnvironment, currentEnvPath: string, portal: Portal }

/** Base page for configuring the content and integrations for a study environment */
function StudyEnvironmentProvider() {
  const params = useParams<StudyParams>()
  const envName: string | undefined = params.studyEnv
  const navContext = useContext(NavbarContext)
  const study = useContext(StudyContext)?.study as Study
  const portal = useContext(PortalContext).portal as Portal

  if (!envName) {
    return <span>no environment selected</span>
  }
  const currentEnv = study.studyEnvironments.find(env => env.environmentName === envName.toLowerCase())
  if (!currentEnv) {
    return <span>invalid environment {envName}</span>
  }

  const currentEnvPath = `/${portal.shortcode}/studies/${study.shortcode}/env/${currentEnv.environmentName}`
  useEffect(() => {
    navContext.setSidebarContent(<StudyEnvironmentSidebar study={study}
      portalShortcode={portal.shortcode}
      currentEnv={currentEnv}
      currentEnvPath={currentEnvPath}
      setShow={navContext.setShowSidebar}/>)
  }, [])

  return <div className="StudyView">
    <NavBreadcrumb>
      <Link className="text-white" to={currentEnvPath}>
        {envName}</Link>
    </NavBreadcrumb>
    <Outlet context={{ study, currentEnv, currentEnvPath, portal }}/>
  </div>
}

/** for child routed components to access the current environment */
export function useStudyEnvironmentOutlet() {
  return useOutletContext<StudyContextT>()
}

export default StudyEnvironmentProvider
