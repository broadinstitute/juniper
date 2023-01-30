import React from 'react'
import { Study, StudyEnvironment } from 'api/api'
import EnvironmentSelector from './EnvironmentSelector'
import { Link, NavLink } from 'react-router-dom'

const sidebarLinkStyle = {
  color: '#fff'
}

/** Sidebar for navigating around configuration of a study environment */
function StudyEnvironmentSidebar({ portalShortcode, study, currentEnv, currentEnvPath, setShow }:
                                   {portalShortcode: string, study: Study, currentEnv: StudyEnvironment | undefined,
                                     currentEnvPath: string, setShow: (show: boolean) => void}) {
  /** returns a full path for the given link leaf */
  function getLinkPath(path: string): string {
    if (!currentEnvPath) {
      return '#'
    }
    return `${currentEnvPath}/${path}`
  }

  /** returns a dynamic style for the link that will highlight when active */
  function getLinkStyle({ isActive }: {isActive: boolean}) {
    return `nav-link ${isActive ? 'active' : ''}`
  }


  return <div className="StudySidebar d-flex flex-column flex-shrink-0 p-3 text-white">
    <h5>
      <Link className="nav-link" to={`/${study.shortcode}`}>{study.name}</Link>
    </h5>
    <hr/>
    <ul className="nav nav-pills flex-column mb-auto">
      <li>
        <label className="form-label">Environment</label>
        <EnvironmentSelector portalShortcode={portalShortcode} study={study} currentEnv={currentEnv}/>
      </li>
      <li>
        <hr/>
      </li>
      <li>
        <NavLink to={getLinkPath('participants')} className={getLinkStyle} onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Participants
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('content')} className={getLinkStyle} onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Content
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('users')} className={getLinkStyle} onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Users
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('theme')} className={getLinkStyle} onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Theme
        </NavLink>
      </li>
      <li>
        <NavLink to={getLinkPath('advanced')} className={getLinkStyle} onClick={() => setShow(false)}
          style={sidebarLinkStyle}>
          Advanced Options
        </NavLink>
      </li>
    </ul>
  </div>
}


export default StudyEnvironmentSidebar
