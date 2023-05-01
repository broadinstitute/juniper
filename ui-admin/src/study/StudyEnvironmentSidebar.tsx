import React from 'react'
import { Study, StudyEnvironment } from 'api/api'
import { Link, NavLink } from 'react-router-dom'
import { studyParticipantsPath } from '../portal/PortalRouter'
import BaseSidebar, { sidebarLinkStyle } from 'navbar/BaseSidebar'

/** Sidebar for navigating around configuration of a study environment */
function StudyEnvironmentSidebar({ portalShortcode, study, currentEnv, currentEnvPath, setShow }:
                                   {portalShortcode: string, study: Study, currentEnv: StudyEnvironment,
                                     currentEnvPath: string, setShow: (show: boolean) => void}) {
  return <div className="StudySidebar d-flex flex-column flex-shrink-0 p-3 text-white">
    <h5>
      <Link className="nav-link" to={currentEnvPath}>{study.name} - { currentEnv.environmentName }</Link>
    </h5>
    <hr/>
    <ul className="nav nav-pills flex-column mb-auto">
      <li>
        <NavLink to={currentEnvPath} className="nav-link" onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Content
        </NavLink>
      </li>
      <li>
        <NavLink to={studyParticipantsPath(portalShortcode, currentEnv.environmentName, study.shortcode)}
          className="nav-link" onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Participants
        </NavLink>
      </li>
    </ul>
    <BaseSidebar setShow={setShow}/>
  </div>
}


export default StudyEnvironmentSidebar
