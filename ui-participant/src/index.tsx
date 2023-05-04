import React from 'react'
import ReactDOM from 'react-dom/client'
import 'bootstrap'
import 'survey-core/defaultV2.min.css'
import './index.scss'
import './surveyJsStyle.css'
import App from './App'
import reportWebVitals from './reportWebVitals'
import PortalEnvrionmentProvider from './providers/PortalProvider'
import setupErrorLogger, { logVitals } from './util/loggingUtils'


const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
)
root.render(<PortalEnvrionmentProvider>
  <App/>
</PortalEnvrionmentProvider>)

setupErrorLogger()

// This reports performance stats using LoggingService.  This is probably far noisier than we need for our application,
// so we might want to look at random sampling
reportWebVitals(logVitals)
