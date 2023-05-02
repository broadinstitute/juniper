import React, { useContext } from 'react'
import { NotificationConfig, Portal, Study, StudyEnvironment } from 'api/api'
import { StudyParams } from 'study/StudyRouter'

import { Link, Route, Routes, useParams } from 'react-router-dom'
import { NavBreadcrumb, SidebarContent } from '../navbar/AdminNavbar'
import { NavbarContext } from '../navbar/NavbarProvider'
import StudyEnvironmentSidebar from './StudyEnvironmentSidebar'
import { PortalContext } from '../portal/PortalProvider'
import SurveyView from './surveys/SurveyView'
import ConsentView from './surveys/ConsentView'
import PreEnrollView from './surveys/PreEnrollView'
import StudyContent from './StudyContent'
import ParticipantsRouter from './participants/ParticipantsRouter'
import NotificationConfigView from './notifications/NotificationConfigView'
import QuestionScratchbox from './surveys/editor/QuestionScratchbox'
import ExportDataBrowser from './participants/export/ExportDataBrowser'


export type StudyEnvContextT = { study: Study, currentEnv: StudyEnvironment, currentEnvPath: string, portal: Portal }

/** Base page for configuring the content and integrations for a study environment */
function StudyEnvironmentRouter({ study }: {study: Study}) {
  const params = useParams<StudyParams>()
  const envName: string | undefined = params.studyEnv
  const navContext = useContext(NavbarContext)
  const portal = useContext(PortalContext).portal as Portal

  if (!envName) {
    return <span>no environment selected</span>
  }
  const currentEnv = study.studyEnvironments.find(env => env.environmentName === envName.toLowerCase())
  if (!currentEnv) {
    return <span>invalid environment {envName}</span>
  }

  const currentEnvPath = `/${portal.shortcode}/studies/${study.shortcode}/env/${currentEnv.environmentName}`

  const studyEnvContext: StudyEnvContextT = { study, currentEnv, currentEnvPath, portal }
  return <div className="StudyView">
    <NavBreadcrumb>
      <Link className="text-white" to={currentEnvPath}>
        {envName}</Link>
    </NavBreadcrumb>
    <SidebarContent>
      <StudyEnvironmentSidebar study={study}
        portalShortcode={portal.shortcode}
        currentEnv={currentEnv}
        currentEnvPath={currentEnvPath}
        setShow={navContext.setShowSidebar}/>
    </SidebarContent>
    <Routes>
      <Route path="surveys">
        <Route path=":surveyStableId">
          <Route index element={<SurveyView studyEnvContext={studyEnvContext}/>}/>
        </Route>
        <Route path="scratch" element={<QuestionScratchbox/>}/>
        <Route path="*" element={<div>Unknown survey page</div>}/>
      </Route>
      <Route path="consentForms">
        <Route path=":consentStableId">
          <Route index element={<ConsentView studyEnvContext={studyEnvContext}/>}/>
        </Route>
        <Route path="*" element={<div>Unknown consent page</div>}/>
      </Route>
      <Route path="notificationConfigs/:configId"
        element={<NotificationConfigView studyEnvContext={studyEnvContext}/>}/>
      <Route path="preEnroll">
        <Route path=":surveyStableId" element={<PreEnrollView studyEnvContext={studyEnvContext}/>}/>
        <Route path="*" element={<div>Unknown prereg page</div>}/>
      </Route>
      <Route path="participants/*" element={<ParticipantsRouter studyEnvContext={studyEnvContext}/>}/>
      <Route path="export/dataBrowser" element={<ExportDataBrowser studyEnvContext={studyEnvContext}/>}/>
      <Route index element={<StudyContent studyEnvContext={studyEnvContext}/>}/>
      <Route path="*" element={<div>Unknown study environment page</div>}/>
    </Routes>
  </div>
}

export default StudyEnvironmentRouter

export const notificationConfigPath = (config: NotificationConfig, currentEnvPath: string) => {
  return `${currentEnvPath}/notificationConfigs/${config.id}`
}

export const getExportDataBrowserPath = (currentEnvPath: string) => {
  return `${currentEnvPath}/export/dataBrowser`
}
