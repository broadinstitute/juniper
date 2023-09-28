import React from 'react'
import { Link, Route, Routes } from 'react-router-dom'
import ParticipantList from './participantList/ParticipantList'

import {StudyEnvContextT, StudyEnvParams} from '../StudyEnvironmentRouter'
import EnrolleeView from './enrolleeView/EnrolleeView'
import { NavBreadcrumb } from 'navbar/AdminNavbar'

/** routes to list or individual enrollee view as appropriate */
export default function ParticipantsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      <Link to={`${studyEnvContext.currentEnvPath}/participants`}>
          participants</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":enrolleeShortcode/*" element={<EnrolleeView studyEnvContext={studyEnvContext}/>}/>
      <Route index element={<ParticipantList studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown participant page</div>}/>
    </Routes>
  </>
}

export const studyEnvParticipantPath = (studyEnvParams: StudyEnvParams, enrolleeShortcode: string) => {
  const {portalShortcode, studyShortcode, envName} = studyEnvParams
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants/${enrolleeShortcode}`
}
