import React, { lazy } from 'react'
import { Route, Routes } from 'react-router-dom'
import Navbar from '../Navbar'
import HubPage from './HubPage'

import SurveyView from './survey/SurveyView'
import { ParticipantProfile } from 'participant/ParticipantProfile'
import PrintSurveyView from './survey/PrintSurveyView'
import ManageProfiles from '../participant/ManageProfiles'
const KitInstructions = lazy(() => import('./kit/KitInstructions'))

/** Handles url pathing for hub routes (a.k.a participant is signed in) */
export default function HubRouter() {
  return <>
    <Navbar/>
    <Routes>
      <Route path="study/:studyShortcode/enrollee/:enrolleeShortcode/consent/:stableId/:version"
        element={<SurveyView/>}/>
      <Route
        path="study/:studyShortcode/enrollee/:enrolleeShortcode/consent/:stableId/:version/print"
        element={<PrintSurveyView/>}
      />
      <Route path="study/:studyShortcode/enrollee/:enrolleeShortcode/survey/:stableId/:version"
        element={<SurveyView/>}/>
      <Route
        path="study/:studyShortcode/enrollee/:enrolleeShortcode/survey/:stableId/:version/print"
        element={<PrintSurveyView/>}
      />
      <Route path="study/:studyShortcode/enrollee/:enrolleeShortcode/outreach/:stableId/:version"
        element={<HubPage/>}/>
      <Route index element={<HubPage/>}/>
      <Route path="manageProfiles"
        element={<ManageProfiles/>}/>
      <Route path="profile/:ppUserId?"
        element={<ParticipantProfile/>}/>
      <Route path="kitInstructions"
        element={<KitInstructions/>}/>
      <Route path="*" element={<div>unknown hub route</div>}/>
    </Routes>
  </>
}
