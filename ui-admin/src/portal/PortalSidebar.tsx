import React from 'react'
import { usersPath } from './PortalRouter'
import { SidebarNavLink } from '../navbar/AdminNavbar'

const PortalSidebar = ({ portalShortcode }: {portalShortcode: string}) => {
  return <ul className="nav nav-pills flex-column mb-auto">
    <li>
      <SidebarNavLink to={usersPath(portalShortcode)}>{portalShortcode} users</SidebarNavLink>
    </li>
  </ul>
}

export default PortalSidebar
