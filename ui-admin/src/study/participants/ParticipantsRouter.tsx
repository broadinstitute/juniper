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
import WithdrawnEnrolleeList from './participantList/WithdrawnEnrolleeList'

/** routes to list or individual enrollee view as appropriate */
export default function ParticipantsRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      <Link to={`${studyEnvContext.currentEnvPath}/participants`} className="me-1">
          participants</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="withdrawn" element={<WithdrawnEnrolleeList studyEnvContext={studyEnvContext}/>}/>
      <Route path=":enrolleeShortcodeOrId/*" element={<EnrolleeView studyEnvContext={studyEnvContext}/>}/>
      <Route index element={<ParticipantList studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown participant page</div>}/>
    </Routes>
  </>
}

/** helper to get participant list page path */
export const studyEnvParticipantPath = (studyEnvParams: StudyEnvParams, enrolleeShortcode: string) => {
  const { portalShortcode, studyShortcode, envName } = studyEnvParams
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants/${enrolleeShortcode}`
}
