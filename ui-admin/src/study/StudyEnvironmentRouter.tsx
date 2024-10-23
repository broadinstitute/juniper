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
import DatasetDashboard from './export/datarepo/DatasetDashboard'
import DatasetList from './export/datarepo/DatasetList'
import Select from 'react-select'
import MailingListView from '../portal/MailingListView'
import { ENVIRONMENT_ICON_MAP } from './publishing/PortalPublishingView'
import TriggerList from './notifications/TriggerList'
import SiteContentLoader from '../portal/siteContent/SiteContentLoader'
import AdminTaskList from './adminTasks/AdminTaskList'
import SiteMediaList from '../portal/media/SiteMediaList'
import PreRegView from './surveys/PreRegView'
import {
  ApiProvider,
  I18nProvider,
  StudyEnvParams
} from '@juniper/ui-core'
import DashboardSettings from 'dashboard/DashboardSettings'
import { previewApi } from 'util/apiContextUtils'
import DataImportView from '../portal/DataImportView'
import DataImportList from '../portal/DataImportList'
import FamilyRouter from './families/FamilyRouter'
import { KitScanner } from './kits/kitcollection/KitScanner'
import ExportIntegrationList from './export/integrations/ExportIntegrationList'
import ExportIntegrationView from './export/integrations/ExportIntegrationView'
import ExportIntegrationJobList from './export/integrations/ExportIntegrationJobList'
import LoadedSettingsView from './settings/SettingsView'

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
      <I18nProvider defaultLanguage={'en'} portalShortcode={portal.shortcode}>
        <Routes>
          <Route path="notificationContent/*" element={<TriggerList studyEnvContext={studyEnvContext}
            portalContext={portalContext}/>}/>
          <Route path="participants/*" element={<ParticipantsRouter studyEnvContext={studyEnvContext}/>}/>
          <Route path="families/*" element={<FamilyRouter studyEnvContext={studyEnvContext}/>}/>
          <Route path="kits/scan" element={<KitScanner studyEnvContext={studyEnvContext}/>}/>
          <Route path="kits/*" element={<KitsRouter studyEnvContext={studyEnvContext}/>}/>
          <Route path="siteContent" element={<SiteContentLoader portalEnvContext={portalEnvContext}/>}/>
          <Route path="media" element={<SiteMediaList portalContext={portalContext} portalEnv={portalEnv}/>}/>
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
          <Route path="export/dataRepo/datasets" element={<DatasetList studyEnvContext={studyEnvContext}/>}/>
          <Route path="export/dataRepo/datasets/:datasetName"
            element={<DatasetDashboard studyEnvContext={studyEnvContext}/>}/>
          <Route path="forms">
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
          </Route>
          <Route path="adminTasks">
            <Route index element={<AdminTaskList studyEnvContext={studyEnvContext}/>}/>
          </Route>
          <Route path="*" element={<div>Unknown study environment page</div>}/>
        </Routes>
      </I18nProvider>
    </ApiProvider>
  </div>
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
export const useStudyEnvParamsFromPath = () => {
  const params = useParams<StudyParams & PortalParams>()
  return {
    studyShortcode: params.studyShortcode,
    portalShortcode: params.portalShortcode,
    envName: params.studyEnv
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

/** surveys, consents, etc.. */
export const studyEnvFormsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `/${portalShortcode}/studies/${studyShortcode}/env/${envName}/forms`
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
  return `${currentEnvPath}/notificationContent/triggers/${config.id}`
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
 * helper function for mailing list route -- note the mailing list itself might not be study-specific,
 * but the route is set to maintain study context
 */
export const studyEnvMailingListPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/mailingList`
}

/**
 *
 */
export const studyEnvImportPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/dataImports`
}

/**
 * helper function for mailing list route -- note the site content itself might not be study-specific,
 * but the route is set to maintain study context
 */
export const studyEnvSiteContentPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/siteContent`
}

/**
 * helper function for image manager route -- note the media are portal-scoped, rather than study-scoped,
 * but the route is set to maintain study context
 */
export const studyEnvSiteMediaPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/siteContent`
}

/** helper path for study settings */
export const studyEnvSiteSettingsPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/settings`
}

/** helper for dataset list path */
export const studyEnvDatasetListViewPath = (portalShortcode: string, studyShortcode: string, envName: string) => {
  return `${studyEnvPath(portalShortcode, studyShortcode, envName)}/export/dataRepo/datasets`
}

/** helper for pre registration survey path */
export const studyEnvPreRegPath = (studyEnvParams: StudyEnvParams) => {
  return `${baseStudyEnvPath(studyEnvParams)}/forms/preReg`
}

/** helper for path for particular dataset route */
export const datasetDashboardPath = (datasetName: string, currentEnvPath: string) => {
  return `${currentEnvPath}/export/dataRepo/datasets/${datasetName}`
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

const baseStudyEnvPath = (params: StudyEnvParams) => {
  return `${studyEnvPath(params.portalShortcode,
    params.studyShortcode,
    params.envName)}`
}
