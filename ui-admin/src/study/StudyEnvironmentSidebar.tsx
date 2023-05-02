import React from 'react'
import { Study, StudyEnvironment } from 'api/api'
import { studyParticipantsPath } from '../portal/PortalRouter'
import { SidebarNavLink } from 'navbar/AdminNavbar'

/** Sidebar for navigating around configuration of a study environment */
function StudyEnvironmentSidebar({ portalShortcode, study, currentEnv, currentEnvPath }:
                                   {portalShortcode: string, study: Study, currentEnv: StudyEnvironment,
                                     currentEnvPath: string}) {
  return <div className="StudySidebar d-flex flex-column flex-shrink-0 p-3 text-white">
    <h5>
      <SidebarNavLink to={currentEnvPath}>{study.name} - { currentEnv.environmentName }</SidebarNavLink>
    </h5>
    <hr/>
    <ul className="nav nav-pills flex-column mb-auto">
      <li>
        <SidebarNavLink to={currentEnvPath}>Content</SidebarNavLink>
      </li>
      <li>
        <SidebarNavLink to={studyParticipantsPath(portalShortcode, currentEnv.environmentName, study.shortcode)}>
          Participants
        </SidebarNavLink>
      </li>
    </ul>
  </div>
}


export default StudyEnvironmentSidebar
