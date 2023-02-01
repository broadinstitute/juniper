import React, { useContext } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import 'survey-creator-core/survey-creator-core.min.css'

import './App.css'
import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom'
import { ReactNotifications } from 'react-notifications-component'

import { RedirectFromOAuth } from './login/RedirectFromOAuth'
import { ProtectedRoute } from './login/ProtectedRoute'
import NavbarProvider, { NavbarContext } from 'navbar/NavbarProvider'
import AdminNavbar from 'navbar/AdminNavbar'
import PortalList from 'portal/PortalList'
import PortalProvider from 'portal/PortalProvider'
import PortalDashboard from 'portal/PortalDashboard'

import StudyRouter from 'study/StudyRouter'
import { getOidcConfig } from './authConfig'
import { AuthProvider } from 'react-oidc-context'
import UserProvider from './user/UserProvider'


/** container for the app including the router  */
function App() {
  return (
    <AuthProvider {...getOidcConfig()}>
      <UserProvider>
        <div className="App">
          <ReactNotifications />
          <NavbarProvider>
            <BrowserRouter>
              <Routes>
                <Route element={<ProtectedRoute/>}>
                  <Route path="/" element={<PageFrame/>}>
                    <Route path=":portalShortcode" element={<PortalProvider/>}>
                      <Route path="studies">
                        <Route path=":studyShortcode/*" element={<StudyRouter/>}/>
                      </Route>
                      <Route index element={<PortalDashboard/>}/>
                    </Route>
                    <Route index element={<PortalList/>}/>
                  </Route>
                  <Route path='redirect-from-oauth' element={<RedirectFromOAuth/>}/>
                  <Route path="*" element={<div>Unknown page</div>}/>
                </Route>
              </Routes>
            </BrowserRouter>
          </NavbarProvider>
        </div>
      </UserProvider>
    </AuthProvider>
  )
}

/** Renders the navbar and footer for the page */
function PageFrame() {
  const navContext = useContext(NavbarContext)
  return <div>
    <AdminNavbar {...navContext}/>
    <Outlet/>
  </div>
}
export default App
