import React from 'react'
import {Route, Routes} from 'react-router-dom'
import ConsentView from './consent/ConsentView'
import HubPage from './HubPage'
import HubNavbar from './HubNavbar'

/** Handles url pathing for hub routes (a.k.a participant is signed in) */
export default function HubRouter() {
  return <>
    <HubNavbar/>
    <Routes>
      <Route path="study/:studyShortcode/enrollee/:enrolleeShortcode/consent/:stableId/:version"
             element={<ConsentView/>}/>
      <Route index element={<HubPage/>}/>
      <Route path="*" element={<div>unknown hub route</div>}/>
    </Routes>
  </>
}
