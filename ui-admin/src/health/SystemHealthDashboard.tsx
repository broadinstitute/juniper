import React from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { navDivStyle, navLinkStyleFunc, navListItemStyle } from 'util/subNavStyles'
import { renderPageHeader } from 'util/pageUtils'
import LogEventViewer from './LogEventViewer'

/** shows links to the populate control panels, and handles the routing for them */
export default function SystemHealthDashboard() {
  return <div className="container-fluid">
    { renderPageHeader('System Health') }
    <div className="d-flex">
      <div style={navDivStyle}>
        <ul className="list-unstyled">
          <li style={navListItemStyle} className="ps-3">
            <NavLink to="" end style={navLinkStyleFunc}>Log Events</NavLink>
          </li>
        </ul>
      </div>
      <div className="flex-grow-1 bg-white p-3">
        <LogEventViewer/>
        <Outlet/>
      </div>
    </div>
  </div>
}
