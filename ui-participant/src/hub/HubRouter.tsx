import React from 'react'
import { Route, Routes } from 'react-router-dom'
import Navbar from '../Navbar'
import ConsentView from './consent/ConsentView'
import PrintConsentView from './consent/PrintConsentView'
import HubPage from './HubPage'

import SurveyView from './survey/SurveyView'

/** Handles url pathing for hub routes (a.k.a participant is signed in) */
export default function HubRouter() {
  return <>
    <Navbar/>
    <Routes>
      <Route path="study/:studyShortcode/enrollee/:enrolleeShortcode/consent/:stableId/:version"
        element={<ConsentView/>}/>
      <Route
        path="study/:studyShortcode/enrollee/:enrolleeShortcode/consent/:stableId/:version/print"
        element={<PrintConsentView/>}
      />
      <Route path="study/:studyShortcode/enrollee/:enrolleeShortcode/survey/:stableId/:version"
        element={<SurveyView/>}/>
      <Route index element={<HubPage/>}/>
      <Route path="*" element={<div>unknown hub route</div>}/>
    </Routes>
  </>
}
