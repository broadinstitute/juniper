import React, { useState } from 'react'
import {
  PortalEnvironment,
  PortalEnvironmentConfig,
  StudyEnvironmentConfig
} from 'api/api'
import {
  NavLink,
  Route,
  Routes
} from 'react-router-dom'
import ErrorBoundary from 'util/ErrorBoundary'
import CollapsableMenu from 'navbar/CollapsableMenu'
import {
  navDivStyle,
  navListItemStyle
} from 'util/subNavStyles'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import { WebsiteSettings } from 'study/settings/portal/WebsiteSettings'
import { StudyEnrollmentSettings } from 'study/settings/study/StudyEnrollmentSettings'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { KitSettings } from 'study/settings/study/KitSettings'
import useUpdateEffect from 'util/useUpdateEffect'
import { GeneralPortalSettings } from 'study/settings/portal/GeneralPortalSettings'
import { LanguageSettings } from 'study/settings/portal/LanguageSettings'


/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export function LoadedSettingsView(
  {
    studyEnvContext,
    portalContext
  }:
    {
      studyEnvContext: StudyEnvContextT,
      portalContext: LoadedPortalContextT
    }) {
  const portalEnv = portalContext.portal.portalEnvironments
    .find(env =>
      env.environmentName === studyEnvContext.currentEnv.environmentName) as PortalEnvironment

  const [portalConfig, setPortalConfig] = useState(portalEnv.portalEnvironmentConfig)
  const [hasPortalConfigChanged, setHasPortalConfigChanged] = useState(false)
  const [studyConfig, setStudyConfig] = useState(studyEnvContext.currentEnv.studyEnvironmentConfig)
  const [hasStudyConfigChanged, setHasStudyConfigChanged] = useState(false)

  useUpdateEffect(() => {
    setStudyConfig(studyEnvContext.currentEnv.studyEnvironmentConfig)
  }, [studyEnvContext.currentEnv.environmentName, studyEnvContext.study.shortcode])


  const updatePortalConfig = (field: keyof PortalEnvironmentConfig, val: unknown) => {
    setPortalConfig(old => {
      return { ...old, [field]: val }
    })
    setHasPortalConfigChanged(true)
  }

  const savePortalConfig = async () => {
    // todo: call api
  }


  const updateStudyConfig = (field: keyof StudyEnvironmentConfig, val: unknown) => {
    setStudyConfig(old => {
      return { ...old, [field]: val }
    })
    setHasStudyConfigChanged(true)
  }

  const saveStudyConfig = async () => {
    console.log('saving study config')
    // todo: call api
  }


  return <div className="ParticipantView mt-3 ps-4">
    <div className="row">
      <div className="col-12">
        <h4>
          Site Settings
        </h4>
      </div>
    </div>
    <div className="row mt-2">
      <div className="col-12">
        <div className="d-flex">
          <div style={navDivStyle}>
            <ul className="list-unstyled">
              <li style={navListItemStyle}>
                <CollapsableMenu header={`Portal Settings`} headerClass="text-black" content={
                  <ul className="list-unstyled">
                    <li>
                      <NavLink to="general" className={getLinkCssClasses}>General</NavLink>
                    </li>
                    <li>
                      <NavLink to="website" className={getLinkCssClasses}>Website</NavLink>
                    </li>
                    <li>
                      <NavLink to="languages" className={getLinkCssClasses}>Languages</NavLink>
                    </li>
                  </ul>
                }/>
              </li>
              <li style={navListItemStyle}>
                <CollapsableMenu header={`${studyEnvContext.study.name} Study Settings`} headerClass="text-black"
                  content={
                    <ul className="list-unstyled">
                      <li>
                        <NavLink to={`enrollment`} className={getLinkCssClasses}>Study
                          Enrollment</NavLink>
                      </li>
                      <li>
                        <NavLink to={`kits`} className={getLinkCssClasses}>Kits</NavLink>
                      </li>
                    </ul>}
                />
              </li>
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3 pt-0">
            <ErrorBoundary>
              <Routes>
                <Route path="general" element={<GeneralPortalSettings
                  config={portalConfig}
                  updateConfig={updatePortalConfig}
                  canSave={hasPortalConfigChanged}
                  saveConfig={savePortalConfig}
                  portalContext={portalContext}
                  portalEnv={portalEnv}
                />}/>
                <Route path="languages" element={<LanguageSettings
                  portalContext={portalContext}
                  portalEnv={portalEnv}
                  config={portalConfig}
                  updateConfig={updatePortalConfig}
                  canSave={hasPortalConfigChanged}
                  saveConfig={savePortalConfig}/>}/>

                <Route path="website" element={
                  <WebsiteSettings
                    config={portalConfig}
                    canSave={hasPortalConfigChanged}
                    updateConfig={updatePortalConfig}
                    saveConfig={savePortalConfig}
                  />}
                />
                <Route path="enrollment" element={
                  <StudyEnrollmentSettings
                    studyEnvContext={studyEnvContext}
                    config={studyConfig}
                    canSave={hasStudyConfigChanged}
                    updateConfig={(field, val) => updateStudyConfig(field, val)}
                    saveConfig={() => saveStudyConfig()}
                  />}
                />

                <Route path="kits" element={<KitSettings
                  studyEnvContext={studyEnvContext}
                  portalContext={portalContext}
                  config={studyConfig}
                  canSave={hasStudyConfigChanged}
                  updateConfig={updateStudyConfig}
                  saveConfig={saveStudyConfig}/>}
                />

                {/*<Route index element={<EnrolleeOverview enrollee={enrollee} studyEnvContext={studyEnvContext}*/}
                {/*  onUpdate={onUpdate}/>}/>*/}
                <Route index element={<div>unknown settings route</div>}/>
              </Routes>
            </ErrorBoundary>
          </div>
        </div>
      </div>
    </div>
  </div>
}

/** gets classes to apply to nav links */
function getLinkCssClasses({ isActive }: { isActive: boolean }) {
  return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
}

