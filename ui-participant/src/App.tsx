import React, {useContext} from 'react'

import LandingPage from "landing/LandingPage"
import {BrowserRouter, Route, Routes} from "react-router-dom"
import {PortalEnvironmentContext} from "providers/StudyEnvironmentProvider"
import { PortalEnvironment} from "api/api"


function App() {
  const currentEnv: PortalEnvironment = useContext(PortalEnvironmentContext) as PortalEnvironment
  const homePage = currentEnv.siteContent.localizedSiteContents[0].landingPage

  return (
    <div className="App d-flex flex-column min-vh-100 bg-white">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage homePage={homePage} currentEnv={currentEnv}/>}/>
          <Route path="*" element={<div>unmatched route</div>}/>
        </Routes>
      </BrowserRouter>
    </div>
  )
}


export default App;
