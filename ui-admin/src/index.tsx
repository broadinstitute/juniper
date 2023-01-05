import React from 'react'
import ReactDOM from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'index.css'
import 'fonts/Montserrat.css'
import App from './App'

import reportWebVitals from './reportWebVitals'
import UserProvider from 'user/UserProvider'
import { AuthProvider } from 'react-oidc-context'
import { getOidcConfig } from './authConfig'

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
)

root.render(
  <AuthProvider {...getOidcConfig()}>
    <UserProvider>
      <App/>
    </UserProvider>
  </AuthProvider>
)


// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals()
