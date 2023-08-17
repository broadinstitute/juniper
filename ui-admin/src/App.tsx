import React, { lazy, Suspense } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import './App.css'

import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom'
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
import LoadingSpinner from './util/LoadingSpinner'
import AdminSidebar from './navbar/AdminSidebar'
import NavContextProvider from 'navbar/NavContextProvider'
import PopulateRouteSelect from './populate/PopulateRouteSelect'
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
                <BrowserRouter>
                  <Routes>
                    <Route path="/">
                      <Route path="help/*" element={<Suspense fallback={<LoadingSpinner/>}>
                        <HelpRouter />
                      </Suspense>} />
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
      <div className="flex-grow-1 d-flex flex-column">
        <AdminNavbar/>
        <Outlet/>
      </div>
    </div>
  )
}
export default App
