import React, { useContext } from 'react'
import { Link, Route, Routes, useParams } from 'react-router-dom'
import StudyRouter from '../study/StudyRouter'
import PortalDashboard from './PortalDashboard'
import { LoadedPortalContextT, PortalContext, PortalParams } from './PortalProvider'
import MailingListView from './MailingListView'
import { Portal } from '../api/api'
import { NavBreadcrumb } from '../navbar/AdminNavbar'
import PortalEnvConfigView from './PortalEnvConfigView'

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
      <Route path="config" element={<div>Config not implemented yet</div>}/>
      <Route path="mailingList" element={<MailingListView portalContext={portalContext}
        portalEnv={portalEnv}/>}/>
      <Route index element={<PortalEnvConfigView portalShortcode={portal.shortcode} portalEnv={portalEnv}/>}/>
    </Routes>
  </>
}

export function mailingListPath(portalShortcode: string, envName: string) {
  return `/${portalShortcode}/env/${envName}/mailingList`
}
