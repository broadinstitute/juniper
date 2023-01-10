import React from 'react'
import { Outlet } from 'react-router-dom'
import LandingNavbar from './LandingNavbar'

/** renders the landing page for a portal (e.g. hearthive.org) */
function LandingPageView() {
  return <div className="LandingPage">
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <div>
        <LandingNavbar/>
      </div>
      <div className="flex-grow-1">
        <Outlet/>
      </div>
    </div>
  </div>
}

export default LandingPageView
