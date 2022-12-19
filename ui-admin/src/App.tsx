import React, { useContext, useState } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import 'survey-creator-core/survey-creator-core.min.css'

import './App.css'
import { BrowserRouter, Outlet, Route, Routes } from 'react-router-dom'
import { UserContext, UserContextT } from 'providers/UserProvider'
import { ReactNotifications } from 'react-notifications-component'
import NavbarProvider, { NavbarContext, NavbarContextT } from './navbar/NavbarProvider'
import AdminNavbar from './navbar/AdminNavbar'
import PortalList from './portal/PortalList'

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
                <Route index element={<PortalList/>} />
              </Route>

              <Route path="*" element={<div>Unknown page</div>}/>
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
