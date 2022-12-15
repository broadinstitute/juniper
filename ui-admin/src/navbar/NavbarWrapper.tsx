import React, { useState, useContext } from 'react'
import './Navbar.css'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { UserContext, UserContextT } from 'providers/UserProvider'
import { faBars } from '@fortawesome/free-solid-svg-icons/faBars'

import { Link } from 'react-router-dom'

export type NavbarContextT = {
  menuContent: React.ReactNode | null,
  sidebarContent: React.ReactNode | null,
  showSidebar: false,
  setShowSidebar: (showSidebar: boolean) => void
}

export const NavbarContext = React.createContext<NavbarContextT>({
  menuContent: null,
  sidebarContent: null,
  showSidebar: false,
  setShowSidebar: () => alert('error - navbar not initialized')
})

function NavbarWrapper() {
  const currentUser: UserContextT = useContext(UserContext)
  const menuContent = useState(null)
  const sidebarContent = useState(null)
  const [showSidebar, setShowSidebar] = useState(false)

  let leftButton = <></>
  if (menuContent) {
    leftButton = <button onClick={() => setShowSidebar(!showSidebar)}
      className="btn btn-secondary text-white">
      <FontAwesomeIcon icon={faBars}/>
    </button>
  }


  return <nav className="Navbar navbar navbar-expand-lg navbar-light">
    <div className="container-fluid">
      <div className="d-flex align-items-center">
        <>{ leftButton }
          <Link className="navbar-brand ms-2 fw-bold" to="/">
          Pearl
          </Link>
          { showSidebar && sidebarContent }</>
      </div>
      <div className="collapse navbar-collapse" id="navbarNavDropdown">
        <ul className="navbar-nav ms-auto">
          {!currentUser.user.isAnonymous && <li className="nav-item dropdown">
            <a className="nav-link dropdown-toggle" href="#"
              role="button" data-bs-toggle="dropdown" aria-expanded="false">
              {currentUser.user.email}
            </a>
            <ul className="dropdown-menu">
              <li><a className="dropdown-item" onClick={currentUser.logoutUser}>Logout</a></li>
            </ul>
          </li>}
        </ul>
      </div>
    </div>
  </nav>
}

export default Navbar
