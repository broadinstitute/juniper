import React, { useContext } from 'react'
import 'react-notifications-component/dist/theme.css'
import 'styles/notifications.css'
import 'survey-core/defaultV2.min.css'
import 'survey-creator-core/survey-creator-core.min.css'

import './App.css'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { UserContext, UserContextT } from 'providers/UserProvider'
import { ReactNotifications } from 'react-notifications-component'

function App() {
  const currentUser: UserContextT = useContext(UserContext)

  return (
    <div className="App">
      <ReactNotifications />
      { currentUser.user.isAnonymous && <div className="App-splash-background"/> }
      { !currentUser.user.isAnonymous &&
          <BrowserRouter>
            <Routes>
              <Route index element={<div> You made it</div>} />
              <Route path="*" element={<div>Unknown page</div>}/>
            </Routes>
          </BrowserRouter>
      }
    </div>
  )
}

export default App
