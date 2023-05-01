import React from 'react'
import { useUser } from '../user/UserProvider'
import { NavLink } from 'react-router-dom'

const BaseSidebar = ({ setShow }: {setShow: (show: boolean) => void}) => {
  const { user } = useUser()
  return <ul className="nav nav-pills flex-column mb-auto">
    {user.superuser && <li>
      <NavLink to="/users" className="nav-link" onClick={() => setShow(false)}
        style={sidebarLinkStyle}>
        Users
      </NavLink>
    </li> }
  </ul>
}

export default BaseSidebar

export const sidebarLinkStyle = {
  color: '#fff'
}
