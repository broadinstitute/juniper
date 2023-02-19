import React from 'react'
import {NavLink} from 'react-router-dom'
import Api, {getImageUrl} from 'api/api'
import {usePortalEnv} from 'providers/PortalProvider'
import {useUser} from '../providers/UserProvider'

/** renders the navbar for participant hub page */
export default function HubNavbar() {
  const {localContent} = usePortalEnv()
  const {user, logoutUser} = useUser()

  /** send a logout to the api then logout */
  function doLogout() {
    Api.logout().then(() => {
      logoutUser()
      window.location.href = '/'
    }).catch(e => {
      alert(`an error occurred during logout ${e}`)
    })
  }

  return <nav className="LandingNavbar navbar navbar-expand-lg navbar-light">
    <div className="container-fluid">
      <NavLink to="/hub" className="navbar-brand">
        <img className="Navbar-logo" style={{maxHeight: '30px'}}
             src={getImageUrl(localContent.navLogoCleanFileName, localContent.navLogoVersion)} alt="logo"/>
      </NavLink>
      <div className="collapse navbar-collapse" id="navbarNavDropdown">
        <ul className="navbar-nav ms-auto">
          {user.isAnonymous && <li className="nav-item">
            <NavLink className="nav-link" to="hub">Login</NavLink>
          </li>}
          {!user.isAnonymous && <li className="nav-item dropdown">
            <a className="nav-link dropdown-toggle" href="#"
               role="button" data-bs-toggle="dropdown" aria-expanded="false">
              {user.username}
            </a>
            <ul className="dropdown-menu">
              <li><a className="dropdown-item" onClick={doLogout}>Logout</a></li>
            </ul>
          </li>}
        </ul>
      </div>
    </div>
  </nav>
}
