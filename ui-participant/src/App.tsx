import React from 'react'

import LandingPage from 'landing/LandingPage'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import { NavbarItem } from 'api/api'
import HtmlPageView from 'landing/sections/HtmlPageView'
import PreRegistration from 'landing/registration/Preregistration'
import Registration from 'landing/registration/Registration'
import PortalRegistrationOutlet from './landing/registration/PortalRegistrationOutlet'
import Ineligible from './landing/registration/Ineligible'
import { AuthProvider } from 'react-oidc-context'
import { getOidcConfig } from './authConfig'
import UserProvider from './providers/UserProvider'
import { ProtectedRoute } from './login/ProtectedRoute'
import { RedirectFromOAuth } from './login/RedirectFromOAuth'
import HubPage from './hub/HubPage'
import StudyEnrollOutlet from './studies/enroll/StudyEnrollOutlet'
import PreEnrollView from './studies/enroll/PreEnroll'
import StudyIneligible from './studies/enroll/StudyIneligible'


/**
 * root app -- handles dynamically creating all the routes based on the siteContent
 */
function App() {
  const { localContent, portal } = usePortalEnv()

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
    <Route path="ineligible" element={<Ineligible/>}/>
    <Route path="preReg" element={<PreRegistration/>}/>
    <Route path="register" element={<Registration/>}/>
  </Route>)

  return (
    <AuthProvider {...getOidcConfig()}>
      <UserProvider>
        <div className="App d-flex flex-column min-vh-100 bg-white">
          <BrowserRouter>
            <Routes>
              <Route path="/hub" element={<ProtectedRoute/>}>
                <Route index element={<HubPage/>}/>
              </Route>
              <Route path="/studies/:studyShortcode">
                <Route path="join" element={<StudyEnrollOutlet/>}>
                  <Route path="preEnroll" element={<PreEnrollView/>}/>
                  <Route path="ineligible" element={<StudyIneligible/>}/>
                  <Route path="consent" element={<ProtectedRoute>
                    <div>consent</div>
                  </ProtectedRoute>}/>
                </Route>
                <Route index element={<div>study specific page -- TBD</div>}/>
                <Route path="*" element={<div>unmatched study route</div>}/>
              </Route>
              <Route path="/" element={<LandingPage/>}>
                {landingRoutes}
                <Route path='redirect-from-oauth' element={<RedirectFromOAuth/>}/>
              </Route>
              <Route path="*" element={<div>unmatched route</div>}/>
            </Routes>
          </BrowserRouter>
        </div>
      </UserProvider>
    </AuthProvider>
  )
}


export default App
