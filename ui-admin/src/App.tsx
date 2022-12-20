import React, { useContext } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import 'survey-creator-core/survey-creator-core.min.css'

import './App.css'
import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom'
import { UserContext, UserContextT } from 'user/UserProvider'
import { ReactNotifications } from 'react-notifications-component'

import NavbarProvider, { NavbarContext } from 'navbar/NavbarProvider'
import AdminNavbar from 'navbar/AdminNavbar'
import PortalList from 'portal/PortalList'
import PortalProvider from 'portal/PortalProvider'
import PortalDashboard from 'portal/PortalDashboard'
import StudyDashboard from 'study/StudyDashboard'
import RoutableStudyProvider from './study/StudyProvider'
import StudyEnvironmentProvider from './study/StudyEnvironmentProvider'
import StudyContent from './study/StudyContent'
import SurveyView from './study/surveys/SurveyView'

/** container for the app including the router  */
function App() {
  const currentUser: UserContextT = useContext(UserContext)
  return (
    <div className="App">
      <ReactNotifications />
      <NavbarProvider>
        { currentUser.user.isAnonymous && <div className="App-splash-background"/> }
        { !currentUser.user.isAnonymous &&
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<PageFrame/>}>
                <Route path=":portalShortcode" element={<PortalProvider/>}>
                  <Route path="studies">
                    <Route path=":studyShortcode" element={<RoutableStudyProvider/>}>
                      <Route path="env/:studyEnv" element={<StudyEnvironmentProvider/>}>
                        <Route path="surveys">
                          <Route path=":surveyStableId">
                            <Route index element={<SurveyView/>}/>
                          </Route>
                          <Route path="*" element={<div>Unknown survey page</div>}/>
                        </Route>
                        <Route index element={<StudyContent/>}/>
                      </Route>
                      <Route index element={<StudyDashboard/>} />
                      <Route path="*" element={<div>Unknown study route</div>}/>
                    </Route>
                    <Route path="*" element={<div>Unknown studies route</div>}/>
                  </Route>
                  <Route index element={<PortalDashboard/>} />
                </Route>
                <Route index element={<PortalList/>} />
                <Route path="*" element={<div>Unknown page</div>}/>
              </Route>
            </Routes>
          </BrowserRouter>
        }
      </NavbarProvider>
    </div>
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
