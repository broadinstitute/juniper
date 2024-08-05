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
import { IdleStatusMonitor } from 'login/IdleStatusMonitor'
import { ApiProvider, I18nProvider } from '@juniper/ui-core'
import { BrandConfiguration, brandStyles } from './util/brandUtils'
import { isBrowserCompatible } from './util/browserCompatibilityUtils'
import InvitationPage from './landing/registration/InvitationPage'
import AuthError from './login/AuthError'
import ActiveUserProvider from './providers/ActiveUserProvider'
import { CookieAlert } from './CookieAlert'
import PageNotFound from './PageNotFound'

import { initializeMixpanel } from './util/mixpanelUtils'

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

// The actual project token is injected into the event by the backend
initializeMixpanel('placeholder-token')

/**
 * root app -- handles dynamically creating all the routes based on the siteContent
 */
function App() {
  const [cookiesAcknowledged, setCookiesAcknowledged] = useCookiesAcknowledged()
  const { localContent, portal, portalEnv } = usePortalEnv()

  useEffect(() => {
    const isCompatible = isBrowserCompatible()
    if (!isCompatible) {
      alert('Your browser does not support this page. ' +
        'Please use the latest version of Chrome, Safari, Firefox, Edge, or Android')
    }
  }, [])


  const brandConfig: BrandConfiguration = {}
  if (!localContent) {
    return <div className="alert alert-warning">
      No content has been configured for this language.
      <button className='btn btn-outline-secondary ms-2' onClick={
        () => { localStorage.removeItem('selectedLanguage'); window.location.reload() }
      }>Reload with default language</button>
    </div>
  }
  if (localContent.primaryBrandColor) {
    brandConfig.brandColor = localContent.primaryBrandColor
  }

  if (localContent.dashboardBackgroundColor) {
    brandConfig.backgroundColor = localContent.dashboardBackgroundColor
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
  // add routes for portal registration and invitations not tied to a specific study (e.g. 'join HeartHive')
  landingRoutes.push(<Route key="portalReg" path="/join/invitation"
    element={<InvitationPage/>}>
  </Route>)
  landingRoutes.push(<Route key="portalRegInvite" path="/join/*"
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
                      <ActiveUserProvider>
                        <I18nProvider defaultLanguage={portalEnv.portalEnvironmentConfig.defaultLanguage}
                          portalShortcode={portal.shortcode}>
                          <Suspense fallback={<PageLoadingIndicator/>}>
                            <IdleStatusMonitor
                              maxIdleSessionDuration={30 * 60 * 1000} idleWarningDuration={5 * 60 * 1000}/>
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
                              <Route path="/redirect-from-oauth">
                                <Route index element={<RedirectFromOAuth/>}/>
                                <Route path="error" element={<AuthError/>}/>
                              </Route>
                              <Route path="/privacy" element={<PrivacyPolicyPage/>}/>
                              <Route path="/terms/investigator" element={<InvestigatorTermsOfUsePage/>}/>
                              <Route path="/terms/participant" element={<ParticipantTermsOfUsePage/>}/>
                              <Route path="*" element={<PageNotFound/>}/>
                            </Routes>
                          </Suspense>
                          {!cookiesAcknowledged && <CookieAlert onDismiss={() => setCookiesAcknowledged()} />}
                        </I18nProvider>
                      </ActiveUserProvider>
                    </UserProvider>
                  </AuthProvider>
                }
              </ConfigConsumer>
            </ConfigProvider>
          </BrowserRouter>
        </div>
      </PortalPasswordGate>
    </ApiProvider>

  )
}

export default App
