import React from 'react'
import {
  Link,
  Route,
  Routes
} from 'react-router-dom'
import ParticipantList from './participantList/ParticipantList'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import EnrolleeView from './enrolleeView/EnrolleeView'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { StudyEnvParams } from '@juniper/ui-core'

/** routes to list or individual enrollee view as appropriate */
export default function ParticipantsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      <Link to={`${studyEnvContext.currentEnvPath}/participants`} className="me-1">
          participants</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":enrolleeShortcodeOrId/*" element={<EnrolleeView studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<ParticipantList studyEnvContext={studyEnvContext}/>}/>
    </Routes>
  </>
}

/** helper to get participant list page path */
export const studyEnvParticipantPath = (studyEnvParams: StudyEnvParams, enrolleeShortcode: string) => {
  const { portalShortcode, studyShortcode, envName } = studyEnvParams
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants/${enrolleeShortcode}`
}
