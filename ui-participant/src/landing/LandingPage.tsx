import React from 'react'
import { Outlet } from 'react-router-dom'
import LandingNavbar from './LandingNavbar'
import { LocalSiteContent } from 'api/api'
import { HtmlSectionView } from './sections/HtmlPageView'

/** renders the landing page for a portal (e.g. hearthive.org) */
function LandingPageView({ localContent }: { localContent: LocalSiteContent }) {
  return <div className="LandingPage">
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <div>
        <LandingNavbar aria-label="Primary" />
      </div>
      <main className="flex-grow-1">
        <Outlet/>
      </main>
      {localContent.footerSection && <footer>
        <HtmlSectionView section={localContent.footerSection}/>
      </footer>}
    </div>
  </div>
}

export default LandingPageView
