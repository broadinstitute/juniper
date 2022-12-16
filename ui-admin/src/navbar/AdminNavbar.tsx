import React, { useContext } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { UserContext, UserContextT } from 'user/UserProvider'
import { faBars } from '@fortawesome/free-solid-svg-icons/faBars'

import { Link } from 'react-router-dom'
import { NavbarContextT } from './NavbarProvider'

/** note we name this adminNavbar to avoid naming conflicts with bootstrap navbar */
function AdminNavbar({ menuContent, showSidebar, setShowSidebar }: NavbarContextT) {
  const currentUser: UserContextT = useContext(UserContext)

  let leftButton = <></>
  if (menuContent) {
    leftButton = <button onClick={() => setShowSidebar(!showSidebar)} title="sidebar menu"
      className="btn btn-secondary text-white">
      <FontAwesomeIcon icon={faBars}/>
    </button>
  }

  return <nav className="Navbar navbar navbar-expand-lg navbar-light" style={{
    backgroundColor: 'rgb(51, 136, 0)',
    color: '#f6f6f6'
  }}>
    <div className="container-fluid">
      <div className="d-flex align-items-center">
        { leftButton }
        <Link className="navbar-brand ms-2 fw-bold text-white" to="/">
        Pearl
        </Link>
      </div>
      <div className="collapse navbar-collapse" id="navbarNavDropdown">
        <ul className="navbar-nav ms-auto">
          {!currentUser.user.isAnonymous && <li className="nav-item dropdown">
            <a className="nav-link dropdown-toggle text-white" href="#"
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

export default AdminNavbar
