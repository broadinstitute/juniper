import React from 'react'
import { Link, Route, Routes } from 'react-router-dom'
import ParticipantList from './participantList/ParticipantList'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import EnrolleeLoader from './enrolleeView/EnrolleeLoader'
import { NavBreadcrumb } from 'navbar/AdminNavbar'

/** routes to list or individual enrollee view as appropriate */
export default function ParticipantsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <>
    <NavBreadcrumb>
      <Link to={`${studyEnvContext.currentEnvPath}/participants`}>
          participants</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":enrolleeShortcode/*" element={<EnrolleeLoader studyEnvContext={studyEnvContext}/>}/>
      <Route index element={<ParticipantList studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown participant page</div>}/>
    </Routes>
  </>
}
