import React, { useContext } from 'react'

import LandingPage from 'landing/LandingPage'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { PortalContext } from 'providers/PortalProvider'
import {LocalSiteContent, NavbarItem, Portal, PortalEnvironment} from 'api/api'
import HtmlPageView from './landing/sections/HtmlPageView'
import PreRegistration from "./landing/registration/Preregistration";
import Registration from "./landing/registration/Registration";
import RegistrationOutlet from "./landing/registration/RegistrationOutlet";

/**
 * root app -- handles dynamically creating all the routes based on the siteContent
 */
function App() {
  const portal: Portal = useContext(PortalContext) as Portal
  const portalEnv = portal.portalEnvironments[0]
  const localSiteContent: LocalSiteContent = portalEnv.siteContent.localizedSiteContents[0]

  let landingRoutes: JSX.Element[] = []
  if (localSiteContent?.navbarItems) {
    landingRoutes = localSiteContent.navbarItems
      .filter((navItem: NavbarItem) => navItem.navbarItemType === 'INTERNAL')
      .map((navItem: NavbarItem, index: number) => <Route key={index} path={navItem.htmlPage.path}
        element={<HtmlPageView page={navItem.htmlPage}/>}/>)
    landingRoutes.push(
      <Route index key="main" element={<HtmlPageView page={localSiteContent.landingPage}/>}/>
    )
  }
  landingRoutes.push(<Route key="registration" path="study/:studyShortcode/join"
                            element={<RegistrationOutlet portal={portal}/>}>
    <Route path="preReg" element={<PreRegistration/>}/>
    <Route path="register" element={<Registration/>}/>
  </Route>)

  return (
    <div className="App d-flex flex-column min-vh-100 bg-white">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage/>}>
            {landingRoutes}
          </Route>
          <Route path="*" element={<div>unmatched route</div>}/>
        </Routes>
      </BrowserRouter>
    </div>
  )
}


export default App
