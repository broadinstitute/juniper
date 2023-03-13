import React from 'react'
import ReactDOM from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import 'survey-core/modern.min.css'
import './index.css'
import './surveyJsStyle.css'
import App from './App'
import reportWebVitals from './reportWebVitals'
import PortalEnvrionmentProvider from './providers/PortalProvider'


const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
)
root.render(<PortalEnvrionmentProvider>
  <App/>
</PortalEnvrionmentProvider>)

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals()
