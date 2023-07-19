import React, { useContext, useEffect, useRef } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { UserContextT, useUser } from 'user/UserProvider'
import { faBars } from '@fortawesome/free-solid-svg-icons/faBars'

import { Link, NavLink, NavLinkProps } from 'react-router-dom'
import { NavbarContext, NavbarContextT } from './NavbarProvider'
import {faUserCircle} from "@fortawesome/free-solid-svg-icons";

/** note we name this adminNavbar to avoid naming conflicts with bootstrap navbar */
function AdminNavbar({ breadCrumbs, sidebarContent, showSidebar, setShowSidebar }: NavbarContextT) {
  const currentUser: UserContextT = useUser()
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
      backgroundColor: '#333F52',
      color: '#f6f6f6'
    }}>
      <div className="container-fluid">
        <div className="d-flex align-items-center">
          <button onClick={() => setShowSidebar(!showSidebar)} title="sidebar menu" ref={sidebarToggleRef}
            className="btn btn-secondary text-white">
            <FontAwesomeIcon icon={faBars}/>
          </button>
          <Link className="navbar-brand ms-2 fw-bold text-white" to="/">
          Juniper
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
                <FontAwesomeIcon icon={faUserCircle} className="fa-lg"/>
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
      </div>
    </nav>
    {showSidebar && (
      <div
        ref={sidebarRef}
        style={{
          position: 'absolute',
          top: '56px',
          backgroundColor: 'rgb(92, 101, 117)',
          maxWidth: '280px',
          minWidth: '280px',
          height: 'calc(100% - 56px)',
          display: 'flex',
          flexDirection: 'column',
          color: '#f0f0f0',
          zIndex: 80
        }}
      >
        <div className="flex-grow-1">
          {sidebarContent && sidebarContent.map((content, index) => (
            <div key={index}>
              {content}
              <hr/>
            </div>
          ))}
        </div>
        <div className="p-3">
          <SidebarNavLink to="/terms" style={{ display: 'inline' }}>Terms of Use</SidebarNavLink> |{' '}
          <SidebarNavLink to="/privacy" style={{ display: 'inline' }}>Privacy Policy</SidebarNavLink>
        </div>
      </div>
    )}
  </>
}

/**
 * Component for adding a sidebar item when a component is rendered.
 * The content will be removed when the component is.
 * This component does not render anything directly, but is still structured as a component rather than a pure hook
 * so that order rendering will be in-order rather than reversed.  See https://github.com/facebook/react/issues/15281
 * */
export function SidebarContent({ children }: {children: React.ReactNode}) {
  const navContext = useContext(NavbarContext)
  useEffect(() => {
    /** use the setState arg that takes a function to avoid race conditions */
    navContext.setSidebarContent((oldContent: React.ReactNode[]) => {
      return  [...oldContent, children]
    })
    /** return the function that will remove the breadcrumb */
    return () => {
      navContext.setSidebarContent((oldCrumbs: React.ReactNode[]) => {
        return oldCrumbs.slice(0, -1)
      })
    }
  }, [])
  return null
}

/** renders a link in the sidebar with appropriate style and onClick handler to close the sidebar when clicked */
export function SidebarNavLink(props: NavLinkProps) {
  const { setShowSidebar } = useContext(NavbarContext)
  return (
    <NavLink
      {...props}
      className="nav-link"
      onClick={() => setShowSidebar(false)}
      style={{ ...props.style, color: '#fff' }}
    />
  )
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
