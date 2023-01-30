import React from 'react'
import { Route, Routes } from 'react-router-dom'
import ParticipantList from './ParticipantList'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import EnrolleeRouter from './EnrolleeRouter'

export default function ParticipantsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <Routes>
    <Route path=":enrolleeShortcode/*" element={<EnrolleeRouter studyEnvContext={studyEnvContext}/>}/>
    <Route index element={<ParticipantList studyEnvContext={studyEnvContext}/>}/>
    <Route path="*" element={<div>Unknown participant page</div>}/>
  </Routes>
}
