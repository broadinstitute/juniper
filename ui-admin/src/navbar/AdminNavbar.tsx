import React, { useContext, useEffect, useRef, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { UserContextT, useUser } from 'user/UserProvider'
import { faBars } from '@fortawesome/free-solid-svg-icons/faBars'

import { Link, NavLink, NavLinkProps } from 'react-router-dom'
import { NavbarContext, NavbarContextT } from './NavbarProvider'
import {faChevronRight, faQuestionCircle, faUserCircle} from '@fortawesome/free-solid-svg-icons'
import ContactSupportInfoModal from '../help/ContactSupportInfoModal'

/** note we name this adminNavbar to avoid naming conflicts with bootstrap navbar */
function AdminNavbar({ breadCrumbs }: NavbarContextT) {
  const currentUser: UserContextT = useUser()
  const [showContactModal, setShowContactModal] = useState(false)
  if (!breadCrumbs) {
    breadCrumbs = []
  }

  if (currentUser.user.isAnonymous) {
    return <div></div>
  }
  return <>
    <nav className="Navbar navbar navbar-expand-lg navbar-light">
      <div className="collapse navbar-collapse" id="navbarNavDropdown">
        <ul className="navbar-nav ms-3">
          { breadCrumbs.map((crumb, index) => <li key={index} className="ms-2">
            {crumb} {(index < breadCrumbs.length -1) &&
              <FontAwesomeIcon icon={faChevronRight} className="fa-xs text-muted"/>}
          </li>)}
        </ul>
        <ul className="navbar-nav ms-auto">
          <li className="nav-item dropdown">
            <a className="nav-link" href="#"
              role="button" data-bs-toggle="dropdown" aria-expanded="false">
              <FontAwesomeIcon icon={faQuestionCircle} className="fa-2x text-dark" title="help menu"/>
            </a>
            <div className="dropdown-menu dropdown-menu-end p-3">
              <ul className="list-unstyled">
                <li>
                  <Link className="dropdown-item" to="/help" target="_blank">Help pages</Link>
                  <a className="dropdown-item" onClick={() => setShowContactModal(!showContactModal)}>
                    Contact support
                  </a>
                </li>
              </ul>
            </div>
          </li>
          {!currentUser.user.isAnonymous && <li className="nav-item dropdown">
            <a className="nav-link" href="#"
              role="button" data-bs-toggle="dropdown" aria-expanded="false">
              <FontAwesomeIcon icon={faUserCircle} className="fa-2x text-dark" title="user menu"/>
            </a>
            <div className="dropdown-menu dropdown-menu-end p-3">
              <h3 className="h6">{currentUser.user.username}</h3>
              <hr/>
              <ul className="list-unstyled">
                <li>
                  <a className="dropdown-item" onClick={currentUser.logoutUser}>Logout</a>
                </li>
              </ul>
            </div>
          </li>}
        </ul>
      </div>
    </nav>
    { showContactModal && <ContactSupportInfoModal onHide={() => setShowContactModal(false)}/> }
  </>
}

/**
 * Component for adding a breadcrumb into the navbar when a component is rendered.
 * The breadcrumb will be removed when the component is.
 * This component does not render anything directly, but is still structured as a component rather than a pure hook
 * so that order rendering will be in-order rather than reversed.  See https://github.com/facebook/react/issues/15281
 * */
export function NavBreadcrumb({ children }: {children: React.ReactNode}) {
  const navContext = useContext(NavbarContext)
  useEffect(() => {
    /** use the setState arg that takes a function to avoid race conditions */
    navContext.setBreadCrumbs((oldCrumbs: React.ReactNode[]) => {
      return  [...oldCrumbs, children]
    })
    /** return the function that will remove the breadcrumb */
    return () => {
      navContext.setBreadCrumbs((oldCrumbs: React.ReactNode[]) => {
        return oldCrumbs.slice(0, -1)
      })
    }
  }, [])
  return null
}

export default AdminNavbar
