import React from 'react'
import { useUser } from '../user/UserProvider'
import { SidebarNavLink } from './AdminNavbar'

const BaseSidebar = () => {
  const { user } = useUser()
  return <ul className="nav nav-pills flex-column mb-auto">
    {user.superuser && <li>
      <SidebarNavLink to="/users">All users</SidebarNavLink>
    </li> }
  </ul>
}

export default BaseSidebar
