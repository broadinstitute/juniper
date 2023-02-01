import React from 'react'
import { Link, Route, Routes } from 'react-router-dom'
import ParticipantList from './ParticipantList'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import EnrolleeRouter from './EnrolleeRouter'
import { NavBreadcrumb } from '../../navbar/AdminNavbar'

export default function ParticipantsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <>
    <NavBreadcrumb>
      <Link className="text-white" to={`${studyEnvContext.currentEnvPath}/participants`}>
          participants</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":enrolleeShortcode/*" element={<EnrolleeRouter studyEnvContext={studyEnvContext}/>}/>
      <Route index element={<ParticipantList studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown participant page</div>}/>
    </Routes>
  </>
}
