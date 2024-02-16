import React, { lazy, Suspense, useEffect } from 'react'

import LandingPage from 'landing/LandingPage'
import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import Api, { NavbarItem, NavbarItemInternal } from 'api/api'
import HtmlPageView from 'landing/HtmlPageView'
import PortalRegistrationRouter from 'landing/registration/PortalRegistrationRouter'
import { AuthProvider } from 'react-oidc-context'
import { getAuthProviderProps } from 'authConfig'
import UserProvider from 'providers/UserProvider'
import { ProtectedRoute } from 'login/ProtectedRoute'
import { RedirectFromOAuth } from 'login/RedirectFromOAuth'
import StudyEnrollRouter from 'studies/enroll/StudyEnrollRouter'
import HubRouter from 'hub/HubRouter'
import PortalPasswordGate from 'landing/PortalPasswordGate'
import EnvironmentAlert from 'EnvironmentAlert'
import ConfigProvider, { ConfigConsumer } from 'providers/ConfigProvider'
import { DocumentTitle } from 'util/DocumentTitle'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { useCookiesAcknowledged } from './browserPersistentState'
import { CookieAlert } from './CookieAlert'
import { IdleStatusMonitor } from 'login/IdleStatusMonitor'
import { ApiProvider } from '@juniper/ui-core'
import { ParticipantProfile } from './participant/ParticipantProfile'
import { BrandConfiguration, brandStyles } from './util/brandUtils'

const PrivacyPolicyPage = lazy(() => import('terms/PrivacyPolicyPage'))
const InvestigatorTermsOfUsePage = lazy(() => import('terms/InvestigatorTermsOfUsePage'))
const ParticipantTermsOfUsePage = lazy(() => import('terms/ParticipantTermsOfUsePage'))


const ScrollToTop = () => {
  const location = useLocation()
  useEffect(() => {
    // @ts-expect-error TS thinks "instant" isn't a valid scroll behavior.
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' })
  }, [location.pathname])
  return null
}

/**
 * root app -- handles dynamically creating all the routes based on the siteContent
 */
function App() {
  const [cookiesAcknowledged, setCookiesAcknowledged] = useCookiesAcknowledged()
  const { localContent, portal } = usePortalEnv()

  const brandConfig: BrandConfiguration = {}
  if (localContent.primaryBrandColor) {
    brandConfig.brandColor = localContent.primaryBrandColor
  }

  let landingRoutes: JSX.Element[] = []
  if (localContent.navbarItems) {
    landingRoutes = localContent.navbarItems
      .filter((navItem: NavbarItem): navItem is NavbarItemInternal => navItem.itemType === 'INTERNAL')
      .map((navItem: NavbarItemInternal, index: number) => (
        <Route
          key={index}
          path={navItem.htmlPage.path}
          element={<HtmlPageView page={navItem.htmlPage}/>}
        />
      ))
    landingRoutes.push(
      <Route index key="main" element={<HtmlPageView page={localContent.landingPage}/>}/>
    )
  }
  // add routes for portal registration not tied to a specific study (e.g. 'join HeartHive')
  landingRoutes.push(<Route key="portalReg" path="/join/*"
    element={<PortalRegistrationRouter portal={portal} returnTo="/hub"/>}>
  </Route>)

  return (
    <ApiProvider api={Api}>
      <EnvironmentAlert portalEnvironment={portal.portalEnvironments[0]}/>
      <DocumentTitle />
      <PortalPasswordGate portal={portal}>
        <div
          className="App d-flex flex-column min-vh-100 bg-white"
          style={brandStyles(brandConfig)}
        >
          <BrowserRouter>
            <ScrollToTop />
            <ConfigProvider>
              <ConfigConsumer>
                {config =>
                  <AuthProvider {
                    ...getAuthProviderProps(config.b2cTenantName, config.b2cClientId, config.b2cPolicyName)
                  }>
                    <UserProvider>
                      <Suspense fallback={<PageLoadingIndicator />}>
                        <IdleStatusMonitor maxIdleSessionDuration={30 * 60 * 1000} idleWarningDuration={5 * 60 * 1000}/>
                        <Routes>
                          <Route path="/hub/*" element={<ProtectedRoute><HubRouter/></ProtectedRoute>}/>
                          <Route path="/studies/:studyShortcode">
                            <Route path="join/*" element={<StudyEnrollRouter/>}/>
                            <Route index element={<div>study specific page -- TBD</div>}/>
                            <Route path="*" element={<div>unmatched study route</div>}/>
                          </Route>
                          <Route path="/" element={<LandingPage localContent={localContent}/>}>
                            {landingRoutes}
                          </Route>
                          <Route path="/redirect-from-oauth" element={<RedirectFromOAuth/>}/>
                          <Route path="/privacy" element={<PrivacyPolicyPage />} />
                          <Route path="/terms/investigator" element={<InvestigatorTermsOfUsePage />} />
                          <Route path="/terms/participant" element={<ParticipantTermsOfUsePage />} />
                          <Route path="/participant">
                            <Route path="profile"
                              element={<ProtectedRoute><ParticipantProfile/></ProtectedRoute>}/>
                          </Route>
                          <Route path="*" element={<div>unmatched route</div>}/>
                        </Routes>
                      </Suspense>
                    </UserProvider>
                  </AuthProvider>
                }
              </ConfigConsumer>
            </ConfigProvider>
            {!cookiesAcknowledged && <CookieAlert onDismiss={() => setCookiesAcknowledged()} />}
          </BrowserRouter>
        </div>
      </PortalPasswordGate>
    </ApiProvider>

  )
}

export default App
