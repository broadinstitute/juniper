import React, { useContext } from 'react'
import { Route, Routes, useParams } from 'react-router-dom'
import StudyRouter from '../study/StudyRouter'
import PortalDashboard from './PortalDashboard'
import { LoadedPortalContextT, PortalContext, PortalParams } from './PortalProvider'
import MailingListView from './MailingListView'
import PortalEnvView from './PortalEnvView'
import PortalEnvConfigView from './PortalEnvConfigView'
import PortalUserList from '../user/PortalUserList'
import PortalParticipantsView from './PortalParticipantView'
import { Portal, PortalEnvironment } from '@juniper/ui-core'
import SiteContentLoader from './siteContent/SiteContentLoader'

export type PortalEnvContext = {
  portal: Portal
  updatePortal: (portal: Portal) => void  // this updates the UI -- it does not handle server-side operations
  reloadPortal: (shortcode: string) => Promise<Portal>
  portalEnv: PortalEnvironment
  updatePortalEnv: (portalEnv: PortalEnvironment) => void // this updates the UI -- not server-side operations
}

/** controls routes for within a portal */
export default function PortalRouter() {
  const portalContext = useContext(PortalContext) as LoadedPortalContextT
  return <>
    <Routes>
      <Route path="studies">
        <Route path=":studyShortcode/*" element={<StudyRouter portalContext={portalContext}/>}/>
      </Route>
      <Route path="users" element={<PortalUserList portal={portalContext.portal}/>}/>
      <Route path="env/:portalEnv/*" element={<PortalEnvRouter portalContext={portalContext}/>}/>
      <Route index element={<PortalDashboard portal={portalContext.portal}/>}/>
      <Route path="*" element={<div>Unmatched portal route</div>}/>
    </Routes>
  </>
}

/** controls routes within a portal environment, such as config, mailing list, etc... */
function PortalEnvRouter({ portalContext }: {portalContext: LoadedPortalContextT}) {
  const params = useParams<PortalParams>()
  const portalEnvName: string | undefined = params.portalEnv
  const { portal } = portalContext
  const portalEnv = portal.portalEnvironments.find(env => env.environmentName === portalEnvName)
  if (!portalEnv) {
    return <div>No environment matches {portalEnvName}</div>
  }

  const portalEnvContext: PortalEnvContext = {
    ...portalContext,
    portalEnv
  }

  return <>
    <Routes>
      <Route path="config" element={<PortalEnvConfigView portal={portal} portalEnv={portalEnv}
        updatePortal={portalContext.updatePortal}/>}/>
      <Route path="participants" element={<PortalParticipantsView portalEnv={portalEnv} portal={portal}/>}/>
      <Route path="siteContent" element={<SiteContentLoader portalEnvContext={portalEnvContext}/>}/>
      <Route path="mailingList" element={<MailingListView portalContext={portalContext}
        portalEnv={portalEnv}/>}/>
      <Route index element={<PortalEnvView portal={portal} portalEnv={portalEnv}/>}/>
    </Routes>
  </>
}

/** admin homepage for a given portal */
export const portalHomePath = (portalShortcode: string) => {
  return `/${portalShortcode}`
}

/** path to portal-specific user list */
export const usersPath = (portalShortcode: string) => {
  return `/${portalShortcode}/users`
}

/** gets absolute path to the portal mailing list page */
export const mailingListPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/mailingList`
}

/** path to edit the site content */
export const siteContentPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/siteContent`
}

/** path to env config for the portal */
export const portalConfigPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/config`
}

/** path to study participant list */
export const studyParticipantsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants`
}

/** Construct a path to a study's kit management interface. */
export const studyKitsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/kits`
}

/** view study content, surveys, consents, etc... */
export const studyContentPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}

/** list all participants in all studies for the portal */
export const portalParticipantsPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/participants`
}

