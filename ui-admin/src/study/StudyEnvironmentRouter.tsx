import React, { useContext } from 'react'
import {
  Portal,
  PortalEnvironment,
  Study,
  StudyEnvironment,
  Trigger
} from 'api/api'
import { StudyParams } from 'study/StudyRouter'

import {
  Link,
  Route,
  Routes,
  useNavigate,
  useParams
} from 'react-router-dom'
import { NavBreadcrumb } from '../navbar/AdminNavbar'
import {
  LoadedPortalContextT,
  PortalContext,
  PortalParams
} from '../portal/PortalProvider'
import SurveyView from './surveys/SurveyView'
import PreEnrollView from './surveys/PreEnrollView'
import StudyContent from './StudyContent'
import KitsRouter from './kits/KitsRouter'
import ParticipantsRouter from './participants/ParticipantsRouter'
import QuestionScratchbox from './surveys/editor/QuestionScratchbox'
import ExportDataBrowser from './export/ExportDataBrowser'
import StudyEnvMetricsView from './metrics/StudyEnvMetricsView'
import Select from 'react-select'
import MailingListView from '../portal/MailingListView'
import TriggerList from './notifications/TriggerList'
import AdminTaskList from './adminTasks/AdminTaskList'
import SiteMediaList from '../portal/media/SiteMediaList'
import PreRegView from './surveys/PreRegView'
import {
  ApiProvider,
  ENVIRONMENT_NAMES,
  EnvironmentName,
  I18nProvider,
  OptionalStudyEnvParams,
  StudyEnvParams
} from '@juniper/ui-core'
import DashboardSettings from 'dashboard/DashboardSettings'
import { previewApi } from 'util/apiContextUtils'
import DataImportView from '../portal/DataImportView'
import DataImportList from '../portal/DataImportList'
import FamilyRouter from './families/FamilyRouter'

import WorkflowView from './workflow/WorkflowView'
import { KitScanner } from './kits/kitcollection/KitScanner'
import ExportIntegrationList from './export/integrations/ExportIntegrationList'
import ExportIntegrationView from './export/integrations/ExportIntegrationView'
import ExportIntegrationJobList from './export/integrations/ExportIntegrationJobList'
import LoadedSettingsView from './settings/SettingsView'
import { ENVIRONMENT_ICON_MAP } from 'util/publishUtils'
import SiteContentLoader from '../portal/siteContent/SiteContentLoader'
import PortalDashboard from '../portal/dashboard/PortalDashboard'
import {
  mailingListPath,
  PortalEnvContext
} from '../portal/PortalRouter'
import { PortalAdminUserRouter } from '../user/AdminUserRouter'

export type StudyEnvContextT = { study: Study, currentEnv: StudyEnvironment, currentEnvPath: string, portal: Portal }

/** Base page for configuring the content and integrations for a study environment */
function StudyEnvironmentRouter({ study }: { study: Study }) {
  const params = useParams<StudyParams>()
  const envName: string | undefined = params.studyEnv
  const portalContext = useContext(PortalContext) as LoadedPortalContextT
  const portal = portalContext.portal
  const navigate = useNavigate()

  const changeEnv = (newEnv?: string) => {
    if (!newEnv) {
      return
    }
    const currentPath = window.location.pathname
    const newPath = currentPath
      .replace(`/env/${envName}`, `/env/${newEnv}`)
    navigate(newPath)
  }

  if (!envName) {
    return <span>no environment selected</span>
  }
  const currentEnv = study.studyEnvironments.find(env => env.environmentName === envName.toLowerCase())
  if (!currentEnv) {
    return <span>invalid environment {envName}</span>
  }
  const envOpts = ['live', 'irb', 'sandbox'].map(env => ({
    label: <span>
      {ENVIRONMENT_ICON_MAP[env]} &nbsp; {env}
    </span>, value: env
  }))
  const currentEnvPath = studyEnvPath(portal.shortcode, study.shortcode, currentEnv.environmentName)
  const portalEnv = portal.portalEnvironments
    .find(env => env.environmentName === currentEnv.environmentName) as PortalEnvironment
  const studyEnvContext: StudyEnvContextT = { study, currentEnv, currentEnvPath, portal }
  const portalEnvContext = {
    ...portalContext, portalEnv
  }
  const studyEnvParams = paramsFromContext(studyEnvContext)

  return <div className="StudyView d-flex flex-column flex-grow-1" key={studyEnvContext.currentEnvPath}>
    <NavBreadcrumb value={currentEnvPath}>
      <Select options={envOpts}
        value={envOpts.find(opt => opt.value === envName)}
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
    <ApiProvider api={previewApi(portal.shortcode, currentEnv.environmentName)}>
      <I18nProvider defaultLanguage={'en'} portalShortcode={portal.shortcode}
        environmentName={portalEnv.environmentName as EnvironmentName}>
        <Routes>
          <Route path="triggers/*" element={<TriggerList studyEnvContext={studyEnvContext}
            portalContext={portalContext}/>}/>
          <Route path="workflow" element={<WorkflowView studyEnvContext={studyEnvContext}
            portalContext={portalContext}/>}/>
          <Route path="portalDashboard" element={<PortalDashboard portal={portalContext.portal}/>}/>
          <Route path="users/*" element={<PortalAdminUserRouter portal={portal} studyEnvParams={studyEnvParams}/>}/>
          <Route path="participants/*" element={<ParticipantsRouter studyEnvContext={studyEnvContext}/>}/>
          <Route path="families/*" element={<FamilyRouter studyEnvContext={studyEnvContext}/>}/>
          <Route path="kits/scan" element={<KitScanner studyEnvContext={studyEnvContext}/>}/>
          <Route path="kits/*" element={<KitsRouter studyEnvContext={studyEnvContext}/>}/>
          <Route path="siteContent" element={<SiteContentLoader portalEnvContext={portalEnvContext}/>}/>
          <Route path="media" element={<SiteMediaList portalContext={portalContext} portalEnv={portalEnv}/>}/>
          <Route path="mailingList" element={<MailingListView portalContext={portalContext}
            portalEnv={portalEnv}/>}/>
          <Route path="alerts" element={<DashboardSettings currentEnv={portalEnv}
            portalContext={portalContext}/>}/>
          <Route path="metrics" element={<StudyEnvMetricsView studyEnvContext={studyEnvContext}/>}/>
          <Route path="mailingList" element={<MailingListView portalContext={portalContext}
            portalEnv={portalEnv}/>}/>
          <Route path="dataImports" element={<DataImportList studyEnvContext={studyEnvContext}/>}/>
          <Route path="dataImports/:dataImportId" element={<DataImportView studyEnvContext={studyEnvContext}/>}/>
          <Route path="settings/*" element={<LoadedSettingsView
            studyEnvContext={studyEnvContext}
            portalContext={portalContext}/>}
          />
          <Route path="export/integrations" element={<ExportIntegrationList studyEnvContext={studyEnvContext}/>}/>
          <Route path="export/integrations/:id" element={<ExportIntegrationView studyEnvContext={studyEnvContext}/>}/>
          <Route path="export/integrations/jobs"
            element={<ExportIntegrationJobList studyEnvContext={studyEnvContext}/>}/>

          <Route path="export/dataBrowser" element={<ExportDataBrowser studyEnvContext={studyEnvContext}/>}/>
          <Route path="forms/*" element={<StudyFormsRouter studyEnvContext={studyEnvContext}
            portalEnvContext={portalEnvContext}/>}/>

          <Route path="adminTasks">
            <Route index element={<AdminTaskList studyEnvContext={studyEnvContext}/>}/>
          </Route>
          <Route path="*" element={<div>Unknown study environment page</div>}/>
        </Routes>
      </I18nProvider>
    </ApiProvider>
  </div>
}
const StudyFormsRouter = ({ studyEnvContext, portalEnvContext }:
  {studyEnvContext: StudyEnvContextT, portalEnvContext: PortalEnvContext}) => {
  return <>
    <NavBreadcrumb value={studyEnvFormsParamsPath(paramsFromContext(studyEnvContext))}>
      <Link to={studyEnvFormsParamsPath(paramsFromContext(studyEnvContext))}>Forms</Link>
    </NavBreadcrumb>
    <Routes>
      <Route path="preReg" element={<PreRegView studyEnvContext={studyEnvContext}
        portalEnvContext={portalEnvContext}/>}/>
      <Route path="preEnroll">
        <Route path=":surveyStableId" element={<PreEnrollView studyEnvContext={studyEnvContext}/>}/>
        <Route path="*" element={<div>Unknown preEnroll page</div>}/>
      </Route>
      <Route path="surveys">
        <Route path=":surveyStableId">
          <Route path=":version" element={<SurveyView studyEnvContext={studyEnvContext}/>}/>
          <Route index element={<SurveyView studyEnvContext={studyEnvContext}/>}/>
        </Route>
        <Route path="scratch" element={<QuestionScratchbox/>}/>
        <Route path="*" element={<div>Unknown survey page</div>}/>
      </Route>
      <Route index element={<StudyContent studyEnvContext={studyEnvContext}/>}/>
    </Routes>
  </>
}

export default StudyEnvironmentRouter

/** helper function to get params to pass to API functions */
export const paramsFromContext = (studyEnvContext: StudyEnvContextT): StudyEnvParams => {
  return {
    studyShortcode: studyEnvContext.study.shortcode,
    portalShortcode: studyEnvContext.portal.shortcode,
    envName: studyEnvContext.currentEnv.environmentName
  }
}

/** gets the current study environment from the url.  It's up to the caller to handle if any of the params are
 * not present.  If the caller knows the params will be there, the return can be cast to StudyEnvParams */
export const useStudyEnvParamsFromPath = (): OptionalStudyEnvParams => {
  const params = useParams<StudyParams & PortalParams>()
  const envName = params.studyEnv
  if (envName && !ENVIRONMENT_NAMES.includes(envName as EnvironmentName)) {
    throw new Error(`invalid environment name in url: ${envName}`)
  }
  return {
    studyShortcode: params.studyShortcode,
    portalShortcode: params.portalShortcode!,
    envName: params.studyEnv as EnvironmentName
  }
}

/** helper for participant list path */
export const participantListPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants`
}

export const participantAccountsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/participants/accounts`
}

/** root study environment path */
export const studyEnvPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}

/** root study environment path */
export const studyEnvPathFromParams = (studyEnvParams: StudyEnvParams) => {
  return studyEnvPath(studyEnvParams.portalShortcode, studyEnvParams.studyShortcode, studyEnvParams.envName)
}

export const portalEnvPath = (portalShortcode: string, envName: string) => {
  return `/${portalShortcode}/env/${envName}`
}

/** surveys, consents, etc.. */
export const studyEnvFormsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/forms`
}

/** convenience for the above method from study params */
export const studyEnvFormsParamsPath = (studyEnvParams: StudyEnvParams) => {
  return studyEnvFormsPath(studyEnvParams.portalShortcode, studyEnvParams.studyShortcode, studyEnvParams.envName)
}


/** surveys, consents, etc.. */
export const studyEnvSurveyPath = (studyEnvParams: StudyEnvParams, stableId: string, version: number) => {
  return `${studyEnvFormsPath(studyEnvParams.portalShortcode, studyEnvParams.studyShortcode, studyEnvParams.envName)}`
    + `/surveys/${stableId}/${version}`
}

/** pre-enroll survey */
export const studyEnvPreEnrollPath = (studyEnvParams: StudyEnvParams, stableId: string) => {
  return `/${studyEnvParams.portalShortcode}/studies/`
    + `${studyEnvParams.studyShortcode}/env/${studyEnvParams.envName}/forms/preEnroll/${stableId}`
}

/** helper for path to configure study workflow and triggers */
export const studyEnvWorkflowPath = (studyEnvParams: StudyEnvParams) => {
  return `${studyEnvPathFromParams(studyEnvParams)}/workflow`
}

/** helper for path to configure study workflow and triggers */
export const studyEnvTriggersPath = (studyEnvParams: StudyEnvParams) => {
  return `${studyEnvPathFromParams(studyEnvParams)}/triggers`
}

/** helper for path to configure study workflow and triggers */
export const studyEnvTriggerPath = (studyEnvParams: StudyEnvParams, trigger: Trigger) => {
  return `${studyEnvPathFromParams(studyEnvParams)}/triggers/${trigger.id}`
}

/** helper for path to configure study notifications */
export const studyEnvNotificationsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/notificationContent`
}

/** helper for path to configure participant dashboard alerts */
export const studyEnvAlertsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/alerts`
}

/** path for viewing a particular notification config path */
export const triggerPath = (config: Trigger, currentEnvPath: string) => {
  return `${currentEnvPath}/triggers/${config.id}`
}

/** path to the export preview */
export const studyEnvDataBrowserPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/export/dataBrowser`
}

/** path to the export integration configs */
export const studyEnvExportIntegrationsPath = (studyEnvParams: StudyEnvParams) => {
  return `${baseStudyEnvPath(studyEnvParams)}/export/integrations`
}

/** path to the export integration configs */
export const studyEnvExportIntegrationPath = (studyEnvParams: StudyEnvParams, id: string) => {
  return `${studyEnvExportIntegrationsPath(studyEnvParams)}/${id}`
}

export const studyEnvExportIntegrationJobsPath = (studyEnvParams: StudyEnvParams) => {
  return `${studyEnvExportIntegrationsPath(studyEnvParams)}/jobs`
}


/** helper function for metrics route */
export const studyEnvMetricsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/metrics`
}

/**
 *
 */
export const studyEnvImportPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/dataImports`
}

/** helper path for study settings */
export const studyEnvSiteSettingsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/settings`
}

/** helper for pre registration survey path */
export const studyEnvPreRegPath = (studyEnvParams: StudyEnvParams) => {
  return `${baseStudyEnvPath(studyEnvParams)}/forms/preReg`
}

/** helper for path to admin task list page */
export const adminTasksPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/adminTasks`
}

/**
 * helper for getting paths to family pages
 */
export const familyPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/families`
}

/**
 * viewing publishing history
 */
export const portalPublishHistoryPath = (portalShortcode: string, studyShortcode: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/publishing/history`
}

/** below are duplicates of portal-level routes, but with the current study preserved */
export const studyEnvMailingListPath = (studyEnvParams: OptionalStudyEnvParams) => {
  if (!studyEnvParams.studyShortcode || !studyEnvParams.envName) {
    return `/${mailingListPath(studyEnvParams.portalShortcode, 'live')}`
  }
  return `${baseStudyEnvPath(studyEnvParams as StudyEnvParams)}/mailingList`
}

export const studyEnvSiteContentPath = (studyEnvParams: StudyEnvParams) => {
  return `${baseStudyEnvPath(studyEnvParams)}/siteContent`
}

export const studyEnvSiteMediaPath = (studyEnvParams: StudyEnvParams) => {
  return `${baseStudyEnvPath(studyEnvParams)}/media`
}


export const baseStudyEnvPath = (params: StudyEnvParams) => {
  return `${studyEnvPath(params.portalShortcode,
    params.studyShortcode,
    params.envName)}`
}
