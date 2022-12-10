import React from 'react'
import { HtmlPage, LocalSiteContent, PortalEnvironment } from 'api/api'

import { Outlet } from 'react-router-dom'
import HtmlPageView from './sections/HtmlPageView'

function LandingPageView({ localSiteContent, currentEnv }:
                           {localSiteContent: LocalSiteContent, currentEnv: PortalEnvironment}) {
  return <div className="LandingPage">
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <div>
      </div>
      <div className="flex-grow-1">
        <Outlet/>
      </div>
    </div>
  </div>
}

export default LandingPageView
