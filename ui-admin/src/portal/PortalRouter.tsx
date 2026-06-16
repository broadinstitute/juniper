import React, {
  useContext,
  useEffect
} from 'react'
import {
  Link,
  Route,
  Routes,
  useNavigate,
  useParams
} from 'react-router-dom'
import StudyRouter, { studyShortcodeFromPath } from '../study/StudyRouter'
import PortalDashboard from './dashboard/PortalDashboard'
import {
  LoadedPortalContextT,
  PortalContext,
  PortalParams
} from './PortalProvider'
import MailingListView from './MailingListView'
import PortalEnvView from './PortalEnvView'
import PortalParticipantsView from './PortalParticipantView'
import {
  ApiProvider,
  EnvironmentName,
  I18nProvider,
  Portal,
  PortalEnvironment
} from '@juniper/ui-core'
import SiteContentLoader from './siteContent/SiteContentLoader'
import { NavBreadcrumb } from 'navbar/AdminNavbar'
import Select from 'react-select'
import { portalEnvPath } from 'study/StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faChevronRight,
  faHome
} from '@fortawesome/free-solid-svg-icons'
import SiteMediaList from './media/SiteMediaList'
import { previewApi } from 'util/apiContextUtils'
import { ENVIRONMENT_ICON_MAP } from '../util/publishUtils'

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
  const portal = portalContext.portal

  // if there isn't a study selected, default to the first
  const params = useParams()
  const studyShortcode = studyShortcodeFromPath(params['*'])
  const navigate = useNavigate()

  let effectivePath = portalHomePath(portalContext.portal.shortcode, studyShortcode, 'live')
  const currentStudy = portal.portalStudies.find(pStudy =>
    pStudy.study.shortcode === studyShortcode)?.study ||
    portal?.portalStudies.sort((a, b) => a.createdAt - b.createdAt)[0]?.study
  if (!studyShortcode && currentStudy) {
    effectivePath =`/${portal.shortcode}/studies/${currentStudy?.shortcode}/env/live/portalDashboard`
  }

  useEffect(() => {
    if (!studyShortcode && currentStudy) {
      navigate(effectivePath)
    }
  }, [])

  return <>
    <NavBreadcrumb value={effectivePath}>
      <Link className='me-2' to={''}>
        <FontAwesomeIcon icon={faHome}/> Home
      </Link>
      <FontAwesomeIcon icon={faChevronRight} className="fa-xs text-muted me-2"/>
      <Link className='me-2' to={effectivePath}>
        {portalContext.portal.name}
      </Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="studies">
        <Route path=":studyShortcode/*" element={<StudyRouter portalContext={portalContext}/>}/>
      </Route>
      <Route path="env/:portalEnv/*" element={<PortalEnvRouter portalContext={portalContext}/>}/>
      <Route index element={<PortalDashboard portal={portalContext.portal}/>}/>
      <Route path="*" element={<div>Unmatched portal route</div>}/>
    </Routes>
  </>
}

export const usePortalEnvParamsFromPath = () => {
  const params = useParams<PortalParams>()
  return {
    portalShortcode: params.portalShortcode,
    portalEnv: params.portalEnv
  }
}

/** controls routes within a portal environment, such as config, mailing list, etc... */
function PortalEnvRouter({ portalContext }: {portalContext: LoadedPortalContextT}) {
  const params = useParams<PortalParams>()
  const navigate = useNavigate()
  const portalEnvName: string | undefined = params.portalEnv
  const { portal } = portalContext
  const portalEnv = portal.portalEnvironments.find(env => env.environmentName === portalEnvName)
  if (!portalEnv) {
    return <div>No environment matches {portalEnvName}</div>
  }

  const envOpts = ['live', 'irb', 'sandbox'].map(env => ({
    label: <span>
      {ENVIRONMENT_ICON_MAP[env]} &nbsp; {env}
    </span>, value: env
  }))

  const portalEnvContext: PortalEnvContext = {
    ...portalContext,
    portalEnv
  }

  const changeEnv = (newEnv?: string) => {
    if (!newEnv) {
      return
    }
    const currentPath = window.location.pathname
    const newPath = currentPath
      .replace(`/env/${portalEnvName}`, `/env/${newEnv}`)
    navigate(newPath)
  }

  const currentEnvPath = portalEnvPath(portal.shortcode, portalEnvName || '')

  return <>
    <NavBreadcrumb value={currentEnvPath}>
      <Select options={envOpts}
        value={envOpts.find(opt => opt.value === portalEnvName)}
        className="me-2"
        styles={{
          control: baseStyles => ({
            ...baseStyles,
            minWidth: '9em'
          })
        }}
        onChange={opt => changeEnv(opt?.value)}
      />
    </NavBreadcrumb>
    <ApiProvider api={previewApi(portal.shortcode, portalEnv.environmentName)}>
      <I18nProvider
        defaultLanguage={portalEnv.portalEnvironmentConfig.defaultLanguage}
        portalShortcode={portal.shortcode}
        environmentName={portalEnv.environmentName as EnvironmentName}>
        <Routes>
          <Route path="participants" element={<PortalParticipantsView portalEnv={portalEnv} portal={portal}/>}/>
          <Route path="siteContent" element={<SiteContentLoader portalEnvContext={portalEnvContext}/>}/>
          <Route path="media" element={<SiteMediaList portalContext={portalContext} portalEnv={portalEnv}/>}/>
          <Route path="mailingList" element={<MailingListView portalContext={portalContext}
            portalEnv={portalEnv}/>}/>
          <Route index element={<PortalEnvView portal={portal} portalEnv={portalEnv}/>}/>
          <Route path={'*'} element={<div>Unmatched portal environment route</div>}/>
        </Routes>
      </I18nProvider>
    </ApiProvider>
  </>
}

/** admin homepage for a given portal */
export const portalHomePath = (portalShortcode: string, studyShortcode?: string, envName?: string) => {
  if (!studyShortcode || !envName) {
    return `/${portalShortcode}`
  }
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/portalDashboard`
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

export const siteMediaPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}/media`
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

