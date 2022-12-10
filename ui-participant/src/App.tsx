import React, { useContext } from 'react'

import LandingPage from 'landing/LandingPage'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { PortalEnvironmentContext } from 'providers/StudyEnvironmentProvider'
import { NavbarItem, PortalEnvironment } from 'api/api'
import HtmlPageView from './landing/sections/HtmlPageView'


function App() {
  const currentEnv: PortalEnvironment = useContext(PortalEnvironmentContext) as PortalEnvironment
  const localSiteContent = currentEnv.siteContent.localizedSiteContents[0]

  let landingRoutes: any[] = []
  if (localSiteContent?.navbarItems) {
    landingRoutes = localSiteContent.navbarItems
      .filter((navItem: NavbarItem) => navItem.navbarItemType === 'INTERNAL')
      .map((navItem: NavbarItem, index: number) => <Route key={index} path={navItem.htmlPage.path}
        element={<HtmlPageView page={navItem.htmlPage}/>}/>)
    landingRoutes.push(
      <Route index key="main" element={<HtmlPageView page={localSiteContent.landingPage}/>}/>
    )
  }

  return (
    <div className="App d-flex flex-column min-vh-100 bg-white">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage localSiteContent={localSiteContent} currentEnv={currentEnv}/>}>
            {landingRoutes}
          </Route>
          <Route path="*" element={<div>unmatched route</div>}/>
        </Routes>
      </BrowserRouter>
    </div>
  )
}


export default App
