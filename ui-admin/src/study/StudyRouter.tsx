import React  from 'react'
import { Route, Routes, useParams } from 'react-router-dom'
import { Study } from 'api/api'

import { LoadedPortalContextT } from 'portal/PortalProvider'
import StudyEnvironmentRouter from './StudyEnvironmentRouter'
import StudyDashboard from './StudyDashboard'
import PortalEnvDiffProvider from '../portal/publish/PortalEnvDiffProvider'
import PortalPublishingView from './publishing/PortalPublishingView'
import LoadingSpinner from 'util/LoadingSpinner'
import { PortalAdminUserRouter } from 'user/AdminUserRouter'
import PortalChangeHistoryView from '../portal/publish/PortalChangeHistoryView'

export type StudyContextT = {
  updateStudy: (study: Study) => void
  study: Study
}

export type StudyParams = {
  studyShortcode: string,
  studyEnv: string,
}


/** puts a study in a context based on the url route */
export default function StudyRouter({ portalContext }: {portalContext: LoadedPortalContextT}) {
  const params = useParams<StudyParams>()
  const studyShortname: string | undefined = params.studyShortcode

  if (!studyShortname) {
    return <span>No study selected</span>
  }
  return <StudyRouterFromShortcode shortcode={studyShortname} portalContext={portalContext}/>
}


/**
 * For now, this just reads the study from the existing PortalContext
 * eventually, we will want to load studies separately
 */
function StudyRouterFromShortcode({ shortcode, portalContext }:
                       { shortcode: string, portalContext: LoadedPortalContextT}) {
  const matchedPortalStudy = portalContext.portal.portalStudies.find(portalStudy => {
    return portalStudy.study.shortcode === shortcode
  })
  if (!matchedPortalStudy) {
    /**
     * if we can't match it, this is likely because the user has switched studies, but the corresponding
     * portal hasn't loaded yet.  This can happen due to race conditions in how StudyRouter and PortalProvider
     * both listen to urlParams to determine what to load.  As the hub-study-portal relationships continue to mature
     * we may consider a unified studyPortalRouter that can handle everything with more synchronicity.
     */
    return <div><LoadingSpinner/></div>
  }

  const study = matchedPortalStudy?.study

  return <>
    <Routes>
      <Route path="env/:studyEnv/*" element={<StudyEnvironmentRouter study={study}/>}/>
      <Route path="publishing">
        <Route path="diff/:sourceEnvName/:destEnvName" element={
          <PortalEnvDiffProvider portal={portalContext.portal}
            reloadPortal={() => portalContext.reloadPortal(portalContext.portal.shortcode)}
            studyShortcode={study.shortcode}/>}/>
        <Route path="history" element={<PortalChangeHistoryView portal={portalContext.portal}/>}/>
        <Route index element={<PortalPublishingView portal={portalContext.portal}
          studyShortcode={study.shortcode}/>}/>
      </Route>

      <Route path="users/*" element={<PortalAdminUserRouter portal={portalContext.portal}
        study={study}/>}/>
      <Route index element={<StudyDashboard study={study}/>}/>
    </Routes>
  </>
}

/** given a url path, extracts the study shortcode from it, or returns undefined if the path doesn't contain one */
export const studyShortcodeFromPath = (path: string | undefined) => {
  const match = path?.match(/studies\/([^/]+)/)
  return match ? match[1] : undefined
}

/** path to portal-specific user list, but keeps study in-context */
export const studyUsersPath = (portalShortcode: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/users`
}

/** helper for a publishing route that keeps the study env in context */
export const studyPublishingPath = (portalShortcode: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/publishing`
}


/** path for showing the diff between two study environments */
export const studyDiffPath = (portalShortcode: string, studyShortcode: string,
  srcEnvName: string, destEnvName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/publishing/diff/${srcEnvName}/${destEnvName}`
}
