import React, { useEffect } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import './App.css'
import './print.css'

import { BrowserRouter, Outlet, Route, Routes, useLocation } from 'react-router-dom'
import { ReactNotifications } from 'react-notifications-component'

import { RedirectFromOAuth } from 'login/RedirectFromOAuth'
import { ProtectedRoute } from 'login/ProtectedRoute'
import AdminNavbar from 'navbar/AdminNavbar'
import HomePage from 'HomePage'
import PortalProvider from 'portal/PortalProvider'
import UserProvider from 'user/UserProvider'
import ConfigProvider, { ConfigConsumer } from 'providers/ConfigProvider'
import { getOidcConfig } from 'authConfig'
import { AuthProvider } from 'react-oidc-context'
import PortalRouter from './portal/PortalRouter'
import UserList from './user/UserList'
import InvestigatorTermsOfUsePage from './terms/InvestigatorTermsOfUsePage'
import PrivacyPolicyPage from 'terms/PrivacyPolicyPage'
import { IdleStatusMonitor } from 'login/IdleStatusMonitor'
import AdminSidebar from './navbar/AdminSidebar'
import NavContextProvider from 'navbar/NavContextProvider'
import PopulateRouteSelect from './populate/PopulateRouteSelect'

/** auto-scroll-to-top on any navigation */
const ScrollToTop = () => {
  const location = useLocation()
  useEffect(() => {
    // @ts-expect-error TS thinks "instant" isn't a valid scroll behavior.
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' })
  }, [location.pathname])
  return null
}


/** container for the app including the router  */
function App() {
  return (
    <ConfigProvider>
      <ConfigConsumer>
        { config =>
          <AuthProvider {...getOidcConfig(config.b2cTenantName, config.b2cClientId, config.b2cPolicyName)}>
            <UserProvider>
              <div className="App d-flex flex-column min-vh-100">
                <IdleStatusMonitor maxIdleSessionDuration={30 * 60 * 1000} idleWarningDuration={5 * 60 * 1000}/>
                <ReactNotifications />
                <BrowserRouter>
                  <ScrollToTop/>
                  <Routes>
                    <Route path="/">
                      <Route element={<ProtectedRoute>
                        <NavContextProvider><PageFrame/></NavContextProvider>
                      </ProtectedRoute>}>
                        <Route path="populate/*" element={<PopulateRouteSelect/>}/>
                        <Route path="users" element={<UserList/>}/>
                        <Route path=":portalShortcode/*" element={<PortalProvider><PortalRouter/></PortalProvider>}/>
                        <Route index element={<HomePage/>}/>
                      </Route>
                      <Route path="privacy" element={<PrivacyPolicyPage />} />
                      <Route path="terms" element={<InvestigatorTermsOfUsePage />} />
                      <Route path="*" element={<div>Unknown page</div>}/>
                    </Route>
                    <Route path='redirect-from-oauth' element={<RedirectFromOAuth/>}/>
                  </Routes>
                </BrowserRouter>
              </div>
            </UserProvider>
          </AuthProvider>
        }
      </ConfigConsumer>
    </ConfigProvider>
  )
}

/** Renders the navbar and footer for the page */
function PageFrame() {
  return (
    <div className="d-flex">
      <AdminSidebar/>
      <div className="flex-grow-1 d-flex flex-column" style={{ backgroundColor: '#ededed' }}>
        <AdminNavbar/>
        <Outlet/>
      </div>
    </div>
  )
}
export default App
