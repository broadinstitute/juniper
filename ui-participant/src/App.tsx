import { cssVar, parseToRgb, tint } from 'polished'
import React, { CSSProperties, Suspense, lazy, useEffect } from 'react'

import LandingPage from 'landing/LandingPage'
import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import { isInternalLink, NavbarItemInternal } from 'api/api'
import HtmlPageView from 'landing/sections/HtmlPageView'
import PortalRegistrationRouter from 'landing/registration/PortalRegistrationRouter'
import { AuthProvider } from 'react-oidc-context'
import { getOidcConfig } from 'authConfig'
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

const PrivacyPolicyPage = lazy(() => import('terms/PrivacyPolicyPage'))
const InvestigatorTermsOfUsePage = lazy(() => import('terms/InvestigatorTermsOfUsePage'))
const ParticipantTermsOfUsePage = lazy(() => import('terms/ParticipantTermsOfUsePage'))


type BrandConfiguration = {
  brandColor?: string;
}

const brandStyles = (config: BrandConfiguration = {}): CSSProperties => {
  const {
    brandColor = cssVar('--bs-blue') as string
  } = config

  const brandColorRgb = parseToRgb(brandColor)

  return {
    // Custom properties used in index.css.
    '--brand-color': brandColor,
    '--brand-color-rgb': `${brandColorRgb.red}, ${brandColorRgb.green}, ${brandColorRgb.blue}`,
    '--brand-color-contrast': '#fff',
    '--brand-color-shift-10': tint(0.10, brandColor),
    '--brand-color-shift-15': tint(0.15, brandColor),
    '--brand-color-shift-20': tint(0.20, brandColor),
    '--brand-color-shift-90': tint(0.90, brandColor),
    '--brand-link-color': brandColor,
    // Override Bootstrap properties.
    '--bs-link-color': brandColor,
    '--bs-link-hover-color': tint(0.20, brandColor)
  } as CSSProperties
}

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
  const { localContent, portal } = usePortalEnv()

  const brandConfig: BrandConfiguration = {}
  if (localContent.primaryBrandColor) {
    brandConfig.brandColor = localContent.primaryBrandColor
  }

  let landingRoutes: JSX.Element[] = []
  if (localContent.navbarItems) {
    landingRoutes = localContent.navbarItems
      .filter(isInternalLink)
      .map((navItem: NavbarItemInternal, index: number) => <Route key={index} path={navItem.htmlPage.path}
        element={<HtmlPageView page={navItem.htmlPage}/>}/>)
    landingRoutes.push(
      <Route index key="main" element={<HtmlPageView page={localContent.landingPage}/>}/>
    )
  }
  // add routes for portal registration not tied to a specific study (e.g. 'join HeartHive')
  landingRoutes.push(<Route key="portalReg" path="/join/*"
    element={<PortalRegistrationRouter portal={portal} returnTo="/hub"/>}>
  </Route>)

  return (
    <>
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
                  <AuthProvider {...getOidcConfig(config.b2cTenantName, config.b2cClientId, config.b2cPolicyName)}>
                    <UserProvider>
                      <Suspense fallback={<PageLoadingIndicator />}>
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
                          <Route path="*" element={<div>unmatched route</div>}/>
                        </Routes>
                      </Suspense>
                    </UserProvider>
                  </AuthProvider>
                }
              </ConfigConsumer>
            </ConfigProvider>
          </BrowserRouter>
        </div>
      </PortalPasswordGate>
    </>

  )
}

export default App
