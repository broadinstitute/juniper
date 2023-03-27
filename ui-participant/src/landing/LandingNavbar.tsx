import { Collapse } from 'bootstrap'
import classNames from 'classnames'
import React, { useEffect, useId, useRef } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { HashLink } from 'react-router-hash-link'

import Api, { getImageUrl, isInternalAnchorLink, isInternalLink, NavbarItem } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'

const navLinkClasses = 'nav-link fs-5 ms-lg-3'

/** renders the navbar for participant landing page (for not-logged-in participants) */
export default function LandingNavbar() {
  const portalEnv = usePortalEnv()
  const { localContent } = portalEnv
  const { user, logoutUser } = useUser()
  const navLinks = localContent.navbarItems

  const joinPath = portalEnv.portal.portalStudies.length === 1
    ? `/studies/${portalEnv.portal.portalStudies[0].study.shortcode}/join`
    : '/join'

  /** send a logout to the api then logout */
  function doLogout() {
    Api.logout().then(() => {
      logoutUser()
      window.location.href = '/'
    }).catch(e => {
      alert(`an error occurred during logout ${e}`)
    })
  }

  const dropdownRef = useRef<HTMLDivElement | null>(null)
  const location = useLocation()
  useEffect(() => {
    if (dropdownRef.current) {
      Collapse.getInstance(dropdownRef.current)?.hide()
    }
  }, [location.pathname])

  const dropdownId = useId()

  return <nav className="LandingNavbar navbar navbar-expand-lg navbar-light">
    <div className="container-fluid">
      <NavLink to="/" className="navbar-brand">
        <img className="Navbar-logo" style={{ maxHeight: '30px' }}
          src={getImageUrl(localContent.navLogoCleanFileName, localContent.navLogoVersion)} alt="logo"/>
      </NavLink>
      <button
        aria-controls={dropdownId} aria-expanded="false" aria-label="Toggle navigation"
        className="navbar-toggler"
        data-bs-toggle="collapse" data-bs-target={`#${CSS.escape(dropdownId)}`}
        type="button"
      >
        <span className="navbar-toggler-icon"/>
      </button>
      <div ref={dropdownRef} className="collapse navbar-collapse mt-2 mt-lg-0" id={dropdownId}>
        <ul className="navbar-nav">
          {navLinks.map((navLink: NavbarItem, index: number) => <li key={index} className="nav-item">
            <CustomNavLink navLink={navLink}/>
          </li>)}
        </ul>
        <ul className="navbar-nav ms-auto">
          {user.isAnonymous && (
            <>
              <li className="nav-item">
                <NavLink
                  className={classNames(
                    'btn btn-lg btn-outline-primary',
                    'd-flex justify-content-center',
                    'mb-3 mb-lg-0 ms-lg-3'
                  )}
                  to="/hub"
                >
                  Log In
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className={classNames(
                    'btn btn-lg btn-primary',
                    'd-flex justify-content-center',
                    'mb-3 mb-lg-0 ms-lg-3'
                  )}
                  to={joinPath}
                >
                  Join
                </NavLink>
              </li>
            </>
          )}
          {!user.isAnonymous && <li className="nav-item dropdown">
            <a className={classNames(navLinkClasses, 'dropdown-toggle')} href="#"
              role="button" data-bs-toggle="dropdown" aria-expanded="false">
              {user.username}
            </a>
            <ul className="dropdown-menu">
              <li><a className="dropdown-item" onClick={doLogout}>Log Out</a></li>
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

  if (isInternalLink(navLink)) {
    // we require navbar links to be absolute rather than relative links
    return <NavLink to={`/${navLink.htmlPage.path}`} className={navLinkClasses}>{navLink.label}</NavLink>
  } else if (isInternalAnchorLink(navLink)) {
    return <HashLink to={`/${navLink.anchorLinkPath}`} className={navLinkClasses}>{navLink.label}</HashLink>
  } else if (navLink.itemType === 'MAILING_LIST') {
    return <a role="button" className={navLinkClasses} onClick={() => mailingList(navLink)}>{navLink.label}</a>
  } else if (navLink.itemType === 'EXTERNAL') {
    return <a href={navLink.externalLink} className={navLinkClasses} target="_blank">{navLink.label}</a>
  }
  return <></>
}
