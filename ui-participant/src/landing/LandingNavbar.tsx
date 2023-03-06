import React from 'react'
import { NavLink } from 'react-router-dom'
import { HashLink } from 'react-router-hash-link'
import Api, { getImageUrl, NavbarItem } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

/** renders the navbar for participant landing page (for not-logged-in participants) */
export default function LandingNavbar() {
  const { localContent } = usePortalEnv()
  const { user, logoutUser } = useUser()
  const navLinks = localContent.navbarItems

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
      <NavLink to="/" className="navbar-brand">
        <img className="Navbar-logo" style={{ maxHeight: '30px' }}
          src={getImageUrl(localContent.navLogoCleanFileName, localContent.navLogoVersion)} alt="logo"/>
      </NavLink>
      <button
        aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation"
        className="navbar-toggler"
        data-bs-toggle="collapse" data-bs-target="#navbarNavDropdown"
        type="button"
      >
        <span className="navbar-toggler-icon"/>
      </button>
      <div className="collapse navbar-collapse" id="navbarNavDropdown">
        <ul className="navbar-nav">
          {navLinks.map((navLink: NavbarItem, index: number) => <li key={index} className="nav-item">
            <CustomNavLink navLink={navLink}/>
          </li>)}
        </ul>
        <ul className="navbar-nav ms-auto">
          {user.isAnonymous && <li className="nav-item">
            <NavLink className="nav-link ms-3" to="/hub">Login</NavLink>
          </li>}
          {!user.isAnonymous && <li className="nav-item dropdown">
            <a className="nav-link ms-3 dropdown-toggle" href="#"
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

/** renders a single navBarItem. This will likely get split out into subcomponents for each type as they are
 * implemented
 */
export function CustomNavLink({ navLink }: { navLink: NavbarItem }) {
  /** will eventually popup a modal allowing email address entry */
  function mailingList(navLinkObj: NavbarItem) {
    alert(`mailing list ${navLinkObj.label}`)
  }

  if (navLink.itemType === 'INTERNAL') {
    // we require navbar links to be absolute rather than relative links
    return <NavLink to={`/${navLink.htmlPage.path}`} className="nav-link ms-3">{navLink.label}</NavLink>
  } else if (navLink.itemType === 'INTERNAL_ANCHOR') {
    return <HashLink to={`/${navLink.anchorLinkPath}`} className="nav-link ms-3">{navLink.label}</HashLink>
  } else if (navLink.itemType === 'MAILING_LIST') {
    return <a role="button" className="nav-link ms-3" onClick={() => mailingList(navLink)}>{navLink.label}</a>
  } else if (navLink.itemType === 'EXTERNAL') {
    return <a href={navLink.externalLink} className="nav-link ms-3" target="_blank">{navLink.label}</a>
  }
  return <></>
}
