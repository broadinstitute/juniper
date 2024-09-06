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
import { EnvironmentName } from '@juniper/ui-core'
import {
  navDivStyle,
  navListItemStyle
} from 'util/subNavStyles'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import { WebsiteSettings } from 'study/settings/portal/WebsiteSettings'
import { StudyEnrollmentSettings } from 'study/settings/study/StudyEnrollmentSettings'


/** shows a master-detail view for an enrollee with sub views on surveys, tasks, etc... */
export function LoadedSettingsView(
  {
    currentEnv,
    portalContext
  }:
    {
      currentEnv: EnvironmentName,
      portalContext: LoadedPortalContextT
    }) {
  const portalEnv = portalContext.portal.portalEnvironments
    .find(env =>
      env.environmentName === currentEnv) as PortalEnvironment

  const studies = portalContext.portal.portalStudies.map(ps => ps.study)

  const [portalConfig, setPortalConfig] = useState(portalEnv.portalEnvironmentConfig)

  const getStudyEnvsByShortcode = () => {
    const studyConfigsByShortcode: Record<string, StudyEnvironmentConfig> = {}
    portalContext.portal.portalStudies.forEach(ps => {
      ps.study.studyEnvironments.forEach(se => {
        if (se.environmentName === currentEnv) {
          studyConfigsByShortcode[ps.study.shortcode] = se.studyEnvironmentConfig
        }
      })
    })
    return studyConfigsByShortcode
  }

  const [studyConfigsByShortcode, setStudyConfigsByShortcode] = useState(getStudyEnvsByShortcode())

  const updatePortalConfig = (field: keyof PortalEnvironmentConfig, val: unknown) => {
    setPortalConfig(old => {
      return { ...old, [field]: val }
    })
  }

  const savePortalConfig = async () => {
    // todo: call api
  }


  const updateStudyConfig = (shortcode: string, field: keyof StudyEnvironmentConfig, val: unknown) => {
    setStudyConfigsByShortcode(old => {
      return {
        ...old,
        [shortcode]: {
          ...old[shortcode],
          [field]: val
        }
      }
    })
  }

  const saveStudyConfig = async (shortcode: string) => {
    console.log('saving study config', shortcode)
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
                  <NavLink to="website" className={getLinkCssClasses}>Website</NavLink>
                }/>
              </li>

              {studies.map(study => {
                // const studyEnv = getStudyEnv(study)
                const shortcode = study.shortcode
                const name = study.name

                return <li style={navListItemStyle}>
                  <CollapsableMenu header={`${name} Study Settings`} headerClass="text-black" content={
                    <ul className="list-unstyled">
                      <li>
                        <NavLink to={`${shortcode}/enrollment`} className={getLinkCssClasses}>Study Enrollment</NavLink>
                      </li>
                    </ul>}
                  />
                </li>
              })}
            </ul>
          </div>
          <div className="participantTabContent flex-grow-1 bg-white p-3 pt-0">
            <ErrorBoundary>
              <Routes>
                <Route path="website" element={
                  <WebsiteSettings
                    config={portalConfig}
                    updateConfig={updatePortalConfig}
                    saveConfig={savePortalConfig}
                  />}
                />
                {studies.map(study => {
                  const shortcode = study.shortcode
                  return <Route path={shortcode}>
                    <Route path="enrollment" element={
                      <StudyEnrollmentSettings
                        study={study}
                        config={studyConfigsByShortcode[shortcode]}
                        updateConfig={(field, val) => updateStudyConfig(shortcode, field, val)}
                        saveConfig={() => saveStudyConfig(shortcode)}
                      />}
                    />
                  </Route>
                })}

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

