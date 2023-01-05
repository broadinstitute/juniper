import React, { useContext, useEffect, useRef } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { UserContext, UserContextT } from 'user/UserProvider'
import { faBars } from '@fortawesome/free-solid-svg-icons/faBars'

import { Link } from 'react-router-dom'
import { NavbarContext, NavbarContextT } from './NavbarProvider'

/** note we name this adminNavbar to avoid naming conflicts with bootstrap navbar */
function AdminNavbar({ breadCrumbs, sidebarContent, showSidebar, setShowSidebar }: NavbarContextT) {
  const currentUser: UserContextT = useContext(UserContext)
  const sidebarRef = useRef<HTMLDivElement>(null)
  const sidebarToggleRef = useRef<HTMLButtonElement>(null)
  if (!breadCrumbs) {
    breadCrumbs = []
  }

  /** Add a handler so that clicks outside the sidebar hide the sidebar */
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      // exclude clicks inside the sidebar or of the toggle button
      if (sidebarRef.current && !sidebarRef.current.contains(event.target as HTMLElement) &&
          sidebarToggleRef.current && !sidebarToggleRef.current.contains(event.target as HTMLElement)) {
        setShowSidebar(false)
      }
    }
    document.addEventListener('click', handleClickOutside, true)
    return () => {
      document.removeEventListener('click', handleClickOutside, true)
    }
  }, [])


  return <>
    <nav className="Navbar navbar navbar-expand-lg navbar-light" style={{
      backgroundColor: 'rgb(51, 136, 0)',
      color: '#f6f6f6'
    }}>
      <div className="container-fluid">
        <div className="d-flex align-items-center">
          <button onClick={() => setShowSidebar(!showSidebar)} title="sidebar menu" ref={sidebarToggleRef}
            className="btn btn-secondary text-white">
            <FontAwesomeIcon icon={faBars}/>
          </button>
          <Link className="navbar-brand ms-2 fw-bold text-white" to="/">
          Pearl
          </Link>
        </div>
        <div className="collapse navbar-collapse" id="navbarNavDropdown">
          <ul className="navbar-nav">
            <li key="separator">
              |
            </li>
            { breadCrumbs.map((crumb, index) => <li key={index} className="ms-2">
              {crumb} {(index < breadCrumbs.length -1) && <span className="ms-2">&#x2022;</span>}
            </li>)}
          </ul>
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
    {(showSidebar && sidebarContent) && <div style={{
      position: 'absolute',
      top: '56px',
      backgroundColor: 'rgb(92, 101, 117)',
      maxWidth: '280px',
      minWidth: '280px',
      height: 'calc(100% - 56px)',
      color: '#f0f0f0',
      zIndex: 80
    }} ref={sidebarRef}>
      {sidebarContent}
    </div>}
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
