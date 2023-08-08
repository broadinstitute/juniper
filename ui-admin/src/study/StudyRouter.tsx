import React  from 'react'
import { Route, Routes, useParams } from 'react-router-dom'
import { Study } from 'api/api'

import { LoadedPortalContextT } from 'portal/PortalProvider'
import StudyEnvironmentRouter from './StudyEnvironmentRouter'
import StudyDashboard from './StudyDashboard'
import PortalUserList from '../user/PortalUserList'
import PortalEnvDiffProvider from '../portal/publish/PortalEnvDiffProvider'
import StudyPublishingView from './publishing/StudyPublishingView'

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
    return portalStudy.study.shortcode = shortcode
  })
  if (!matchedPortalStudy) {
    return <div>Study could not be loaded or found.</div>
  }

  const study = matchedPortalStudy?.study

  return <>
    <Routes>
      <Route path="env/:studyEnv/*" element={<StudyEnvironmentRouter study={study}/>}/>
      <Route path="diff/:sourceEnvName/:destEnvName" element={
        <PortalEnvDiffProvider portal={portalContext.portal}
          updatePortal={portalContext.updatePortal} studyShortcode={study.shortcode}/>}/>
      <Route path="publishing" element={<StudyPublishingView portal={portalContext.portal}
        studyShortcode={study.shortcode}/>}/>
      <Route path="users" element={<PortalUserList portal={portalContext.portal}/>}/>
      <Route index element={<StudyDashboard study={study}/>}/>
    </Routes>
  </>
}

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
  return `/${portalShortcode}/studies/${studyShortcode}/diff/${srcEnvName}/${destEnvName}`
}
