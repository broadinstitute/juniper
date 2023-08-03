import React, { useContext } from 'react'
import { Link, Route, Routes, useParams } from 'react-router-dom'
import StudyRouter from '../study/StudyRouter'
import PortalDashboard from './PortalDashboard'
import { LoadedPortalContextT, PortalContext, PortalParams } from './PortalProvider'
import MailingListView from './MailingListView'
import { NavBreadcrumb } from '../navbar/AdminNavbar'
import PortalEnvView from './PortalEnvView'
import SiteContentView from './siteContent/SiteContentView'
import PortalEnvConfigView from './PortalEnvConfigView'
import PortalUserList from '../user/PortalUserList'
import PortalParticipantsView from './PortalParticipantView'
import PortalEnvDiffProvider from './publish/PortalEnvDiffProvider'

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
  const { portal, updatePortal } = portalContext
  const portalEnv = portal.portalEnvironments.find(env => env.environmentName === portalEnvName)
  if (!portalEnv) {
    return <div>No environment matches {portalEnvName}</div>
  }

  return <>
    <NavBreadcrumb>
      <Link to={`${portal.shortcode}/env/${portalEnvName}`}>
        {portalEnvName}
      </Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="config" element={<PortalEnvConfigView portal={portal} portalEnv={portalEnv}
        updatePortal={portalContext.updatePortal}/>}/>
      <Route path="participants" element={<PortalParticipantsView portalEnv={portalEnv} portal={portal}/>}/>
      <Route path="siteContent" element={<SiteContentView portalEnv={portalEnv} portalShortcode={portal.shortcode}/>}/>
      <Route path="diff/:sourceEnvName" element={<PortalEnvDiffProvider portal={portal} portalEnv={portalEnv}
        updatePortal={updatePortal}/>}/>
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

/** absolute path for the environment diff page */
export const portalEnvDiffPath = (portalShortcode: string, destEnvName: string, sourceEnvName: string) => {
  return `/${portalShortcode}/env/${destEnvName}/diff/${sourceEnvName}`
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const siteContentPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/siteContent`
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const portalConfigPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/config`
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const studyParticipantsPath = (portalShortcode: string, envName: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants`
}

/** Construct a path to a study's kit management interface. */
export const studyKitsPath = (portalShortcode: string, envName: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/kits`
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const studyContentPath = (portalShortcode: string, envName: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const portalParticipantsPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/participants`
}
