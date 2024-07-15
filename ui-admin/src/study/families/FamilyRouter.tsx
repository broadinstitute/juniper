import React from 'react'
import {
  Link,
  Route,
  Routes
} from 'react-router-dom'
import {
  participantListPath,
  StudyEnvContextT
} from '../StudyEnvironmentRouter'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import { StudyEnvParams } from '@juniper/ui-core'
import FamilyView from './FamilyView'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'

/** routes to family views as appropriate */
export default function FamilyRouter({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { user } = useUser()

  if (!userHasPermission(user, studyEnvContext.portal.id, 'participant_data_edit')) {
    return <div>Permission denied</div>
  }

  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      <Link to={`${participantListPath(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName
      )}?groupByFamily=true`} className="me-1">
        families</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path=":familyShortcode/*" element={<FamilyView studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown family page</div>}/>
    </Routes>
  </>
}

/** helper to get participant list page path */
export const studyEnvFamilyPath = (studyEnvParams: StudyEnvParams, shortcode: string) => {
  const { portalShortcode, studyShortcode, envName } = studyEnvParams
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/families/${shortcode}`
}
