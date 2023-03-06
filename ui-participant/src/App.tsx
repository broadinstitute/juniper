import { cssVar, parseToRgb, tint } from 'polished'
import React, { CSSProperties } from 'react'

import LandingPage from 'landing/LandingPage'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { usePortalEnv } from 'providers/PortalProvider'
import { NavbarItem } from 'api/api'
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
    '--brand-link-color': brandColor,
    // Override Bootstrap properties.
    '--bs-link-color': brandColor,
    '--bs-link-hover-color': tint(0.20, brandColor)
  } as CSSProperties
}

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
  landingRoutes.push(<Route key="portalReg" path="/join/*"
    element={<PortalRegistrationRouter portal={portal} returnTo="/hub"/>}>
  </Route>)

  return (
    <>
      <EnvironmentAlert portalEnvironment={portal.portalEnvironments[0]}/>
      <PortalPasswordGate portal={portal}>
        <ConfigProvider>
          <ConfigConsumer>
            { config =>
              <AuthProvider {...getOidcConfig(config.b2cTenantName, config.b2cClientId)}>
                <UserProvider>
                  <div
                    className="App d-flex flex-column min-vh-100 bg-white"
                    style={brandStyles({
                      brandColor: 'rgb(155, 36, 133)' // TODO: Get brand color from localContent
                    })}
                  >
                    <BrowserRouter>
                      <Routes>
                        <Route path="/hub/*" element={<ProtectedRoute><HubRouter/></ProtectedRoute>}/>
                        <Route path="/studies/:studyShortcode">
                          <Route path="join/*" element={<StudyEnrollRouter/>}/>
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
            }
          </ConfigConsumer>
        </ConfigProvider>
      </PortalPasswordGate>
    </>

  )
}


export default App
