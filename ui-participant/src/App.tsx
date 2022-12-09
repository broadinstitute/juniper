import React, {useContext, useEffect} from 'react'
import './App.css'

import LandingPage from "landing/LandingPage"
import {BrowserRouter, Route, Routes} from "react-router-dom"
import {PortalEnvironmentContext} from "providers/StudyEnvironmentProvider"
import { PortalEnvironment} from "api/api"


function App() {
  const currentEnv: PortalEnvironment = useContext(PortalEnvironmentContext) as PortalEnvironment
  const homePage = currentEnv.siteContent.localizedSiteContents[0].landingPage

  let landingRoutes: any[] = []

  return (
    <div className="App d-flex flex-column min-vh-100">
      <BrowserRouter>
        <Routes>
          <Route path="/termsOfUse" element={<div>Terms of use here</div>}/>,
          <Route path="/privacyPolicy" element={<div>Privacy policy here</div>}/>,


          <Route path="/" element={<LandingPage homePage={homePage} currentEnv={currentEnv}/>}>
            { landingRoutes }
          </Route>
          <Route path="*" element={<div>unmatched route</div>}/>
        </Routes>
      </BrowserRouter>
    </div>
  )
}


export default App;
