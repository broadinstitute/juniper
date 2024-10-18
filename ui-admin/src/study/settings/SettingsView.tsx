import React, { useState } from 'react'
import Api, {
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
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'
import { withStateResetOnEnvChange } from 'util/withStateResetOnEnvChange'


/** shows a url-routable settings page for both the portal and the selected study */
function LoadedSettingsView(
  {
    studyEnvContext,
    portalContext
  }:
    {
      studyEnvContext: StudyEnvContextT,
      portalContext: LoadedPortalContextT
    }) {
  const portal = portalContext.portal
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

  const [isLoadingPortalConfig, setIsLoadingPortalConfig] = useState(false)
  const savePortalConfig = async () => {
    doApiLoad(async () => {
      await Api.updatePortalEnvConfig(portal.shortcode, portalEnv.environmentName, portalConfig)
      Store.addNotification(successNotification('Portal config saved'))
      portalContext.reloadPortal(portalContext.portal.shortcode)
    }, { setIsLoading: setIsLoadingPortalConfig })
  }

  const updateStudyConfig = (field: keyof StudyEnvironmentConfig, val: unknown) => {
    setStudyConfig(old => {
      return { ...old, [field]: val }
    })
    setHasStudyConfigChanged(true)
  }

  const [isLoadingStudyConfig, setIsLoadingStudyConfig] = useState(false)
  const saveStudyConfig = async () => {
    doApiLoad(async () => {
      await Api.updateStudyEnvironmentConfig(portalContext.portal.shortcode,
        studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName, studyConfig)
      Store.addNotification(successNotification('Config saved'))
      portalContext.reloadPortal(portalContext.portal.shortcode)
    }, { setIsLoading: setIsLoadingStudyConfig })
  }

  if (isLoadingStudyConfig || isLoadingPortalConfig) {
    return <LoadingSpinner/>
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
                    <li className={'mb-2'}>
                      <NavLink end to="." className={getLinkCssClasses}>General</NavLink>
                    </li>
                    <li className={'mb-2'}>
                      <NavLink to="website" className={getLinkCssClasses}>Website</NavLink>
                    </li>
                    <li className={'mb-2'}>
                      <NavLink to="languages" className={getLinkCssClasses}>Languages</NavLink>
                    </li>
                  </ul>
                }/>
              </li>
              <li style={navListItemStyle}>
                <CollapsableMenu header={`${studyEnvContext.study.name} Study Settings`} headerClass="text-black"
                  content={
                    <ul className="list-unstyled">
                      <li className={'mb-2'}>
                        <NavLink to={`enrollment`} className={getLinkCssClasses}>Study
                          Enrollment</NavLink>
                      </li>
                      <li className={'mb-2'}>
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
                <Route index element={<SettingsPage
                  title="General Portal Settings"
                  savePortalConfig={savePortalConfig}
                  canSavePortalConfig={hasPortalConfigChanged}
                >
                  <GeneralPortalSettings
                    config={portalConfig}
                    updateConfig={updatePortalConfig}
                    portalContext={portalContext}
                    portalEnv={portalEnv}
                  />
                </SettingsPage>}/>
                <Route path="languages" element={
                  <SettingsPage
                    title='Language Settings'
                    savePortalConfig={savePortalConfig}
                    canSavePortalConfig={hasPortalConfigChanged}
                  >
                    <LanguageSettings
                      portalContext={portalContext}
                      portalEnv={portalEnv}
                      config={portalConfig}
                      updateConfig={updatePortalConfig}
                    />
                  </SettingsPage>}/>

                <Route path="website" element={
                  <SettingsPage
                    title='Website Settings'
                    savePortalConfig={savePortalConfig}
                    canSavePortalConfig={hasPortalConfigChanged}
                  >
                    <WebsiteSettings
                      config={portalConfig}
                      updateConfig={updatePortalConfig}
                    />
                  </SettingsPage>}
                />
                <Route path="enrollment" element={
                  <SettingsPage
                    title='Study Enrollment Settings'
                    saveStudyConfig={saveStudyConfig}
                    canSaveStudyConfig={hasStudyConfigChanged}
                  >
                    <StudyEnrollmentSettings
                      studyEnvContext={studyEnvContext}
                      config={studyConfig}
                      updateConfig={(field, val) => updateStudyConfig(field, val)}
                    />
                  </SettingsPage>}
                />
                <Route path="kits" element={
                  <SettingsPage
                    title='Kit Settings'
                    saveStudyConfig={saveStudyConfig}
                    canSaveStudyConfig={hasStudyConfigChanged}
                  >
                    <KitSettings
                      studyEnvContext={studyEnvContext}
                      portalContext={portalContext}
                      config={studyConfig}
                      updateConfig={updateStudyConfig}
                    />
                  </SettingsPage>}
                />

                <Route index element={<div>unknown settings route</div>}/>
              </Routes>
            </ErrorBoundary>
          </div>
        </div>
      </div>
    </div>
  </div>
}

export default withStateResetOnEnvChange(LoadedSettingsView)

/** gets classes to apply to nav links */
function getLinkCssClasses({ isActive }: { isActive: boolean }) {
  return `${isActive ? 'fw-bold' : ''} d-flex align-items-center`
}

const SettingsPage = ({
  savePortalConfig,
  saveStudyConfig,
  canSavePortalConfig,
  canSaveStudyConfig,
  title,
  children
}: {
  savePortalConfig?: () => void,
  saveStudyConfig?: () => void,
  canSavePortalConfig?: boolean,
  canSaveStudyConfig?: boolean,
  title: string,
  children: React.ReactNode
}) => {
  return <div>
    <h2 className="h4">{title}</h2>
    {children}
    <div>
      {savePortalConfig && <Button
        variant="primary"
        onClick={savePortalConfig}
        disabled={!canSavePortalConfig}
      >
          Save portal settings
      </Button>}
      {saveStudyConfig && <Button
        variant="primary"
        onClick={saveStudyConfig}
        disabled={!canSaveStudyConfig}
      >
          Save study settings
      </Button>}
    </div>
  </div>
}
