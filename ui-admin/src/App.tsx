import React, { lazy, useContext } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import './App.css'

import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom'
import { ReactNotifications } from 'react-notifications-component'

import { RedirectFromOAuth } from 'login/RedirectFromOAuth'
import { ProtectedRoute } from 'login/ProtectedRoute'
import NavbarProvider, { NavbarContext } from 'navbar/NavbarProvider'
import AdminNavbar from 'navbar/AdminNavbar'
import PortalList from 'portal/PortalList'
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
const HelpRouter = lazy(() => import('./help/HelpRouter'))


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
                <NavbarProvider>
                  <BrowserRouter>
                    <Routes>
                      <Route path="/" element={<PageFrame/>}>
                        <Route path="help/*" element={<HelpRouter />} />
                        <Route element={<ProtectedRoute/>}>
                          <Route path="users" element={<UserList/>}/>
                          <Route path=":portalShortcode/*" element={<PortalProvider><PortalRouter/></PortalProvider>}/>
                          <Route index element={<PortalList/>}/>
                        </Route>

                        <Route path="privacy" element={<PrivacyPolicyPage />} />
                        <Route path="terms" element={<InvestigatorTermsOfUsePage />} />
                        <Route path="*" element={<div>Unknown page</div>}/>
                      </Route>
                      <Route path='redirect-from-oauth' element={<RedirectFromOAuth/>}/>
                    </Routes>
                  </BrowserRouter>
                </NavbarProvider>
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
  const navContext = useContext(NavbarContext)
  return (
    <>
      <AdminNavbar {...navContext}/>
      <Outlet/>
    </>
  )
}
export default App
