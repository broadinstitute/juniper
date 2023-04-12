import React, { useContext } from 'react'
import { Link, Route, Routes, useParams } from 'react-router-dom'
import StudyRouter from '../study/StudyRouter'
import PortalDashboard from './PortalDashboard'
import { LoadedPortalContextT, PortalContext, PortalParams } from './PortalProvider'
import MailingListView from './MailingListView'
import { NavBreadcrumb } from '../navbar/AdminNavbar'
import PortalEnvView from './PortalEnvView'
import PortalEnvDiff from './publish/PortalEnvDiff'
import SiteContentView from './siteContent/SiteContentView'
import PortalEnvConfigView from './PortalEnvConfigView'

/** controls routes for within a portal */
export default function PortalRouter() {
  const portalContext = useContext(PortalContext) as LoadedPortalContextT
  return <Routes>
    <Route path="studies">
      <Route path=":studyShortcode/*" element={<StudyRouter portalContext={portalContext}/>}/>
    </Route>
    <Route path="env/:portalEnv/*" element={<PortalEnvRouter portalContext={portalContext}/>}/>
    <Route index element={<PortalDashboard portal={portalContext.portal}/>}/>
    <Route path="*" element={<div>Unmatched portal route</div>}/>
  </Routes>
}

/** controls routes within a portal environment, such as config, mailing list, etc... */
function PortalEnvRouter({ portalContext }: {portalContext: LoadedPortalContextT}) {
  const params = useParams<PortalParams>()
  const portalEnvName: string | undefined = params.portalEnv
  const portal = portalContext.portal
  const portalEnv = portal.portalEnvironments.find(env => env.environmentName === portalEnvName)
  if (!portalEnv) {
    return <div>No environment matches {portalEnvName}</div>
  }

  return <>
    <NavBreadcrumb>
      <Link className="text-white" to={`${portal.shortcode}/env/${portalEnvName}`}>
        {portalEnvName}
      </Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="config" element={<PortalEnvConfigView portal={portal} portalEnv={portalEnv}/>}/>
      <Route path="siteContent" element={<SiteContentView portalEnv={portalEnv}/>}/>
      <Route path="diff/:sourceEnvName" element={<PortalEnvDiff portal={portal} portalEnv={portalEnv}/>}/>
      <Route path="mailingList" element={<MailingListView portalContext={portalContext}
        portalEnv={portalEnv}/>}/>
      <Route index element={<PortalEnvView portal={portal} portalEnv={portalEnv}/>}/>
    </Routes>
  </>
}

/** gets absolute path to the portal mailing list page */
export const mailingListPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/mailingList`
}

/** absolute path for the environment diff page */
export const portalEnvDiffPath = (portalShortcode: string, destEnvName: string, sourceEnvName: string) => {
  return `/${portalShortcode}/env/${destEnvName}/diff/${sourceEnvName}`
}

export const siteContentPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/siteContent`
}

export const portalConfigPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/config`
}

export const studyParticipantsPath = (portalShortcode: string, envName: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants`
}

export const studyContentPath = (portalShortcode: string, envName: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}
