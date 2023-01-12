import React from 'react'

import LandingPage from 'landing/LandingPage'
import {BrowserRouter, Route, Routes} from 'react-router-dom'
import {usePortalEnv} from 'providers/PortalProvider'
import {NavbarItem} from 'api/api'
import HtmlPageView from 'landing/sections/HtmlPageView'
import PreRegistration from 'landing/registration/Preregistration'
import Registration from 'landing/registration/Registration'
import PortalRegistrationOutlet from "./landing/registration/PortalRegistrationOutlet";

/**
 * root app -- handles dynamically creating all the routes based on the siteContent
 */
function App() {
  const {localContent, portal} = usePortalEnv()

  let landingRoutes: JSX.Element[] = []
  if (localContent.navbarItems) {
    landingRoutes = localContent.navbarItems
      .filter((navItem: NavbarItem) => navItem.itemType === 'INTERNAL')
      .map((navItem: NavbarItem, index: number) => <Route key={index} path={navItem.htmlPage.path}
                                                          element={<HtmlPageView page={navItem.htmlPage}/>}/>)
    landingRoutes.push(
      <Route index key="main" element={<HtmlPageView page={localContent.landingPage}/>}/>
    )
  }
  // add routes for portal registration not tied to a specific study (e.g. 'join HeartHive')
  landingRoutes.push(<Route key="portalReg" path="/join"
                            element={<PortalRegistrationOutlet portal={portal}/>}>
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
