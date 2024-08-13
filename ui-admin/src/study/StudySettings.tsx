import React, { useEffect, useState } from 'react'
import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import PortalEnvConfigView from 'portal/PortalEnvConfigView'
import {
  EnvironmentName,
  PortalEnvironment,
  StudyEnvironmentConfig
} from '@juniper/ui-core'
import { doApiLoad } from 'api/api-utils'
import { Button } from 'components/forms/Button'
import { set } from 'lodash/fp'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from 'components/forms/InfoPopup'
import useUpdateEffect from 'util/useUpdateEffect'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'
import {
  DocsKey,
  ZendeskLink
} from '../util/zendeskUtils'
import Select from 'react-select'

/** shows settings for both a study and its containing portal */
export default function StudySettings({ studyEnvContext, portalContext }:
{studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const portalEnv = portalContext.portal.portalEnvironments
    .find(env =>
      env.environmentName === studyEnvContext.currentEnv.environmentName) as PortalEnvironment

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Site Settings') }
    <StudyEnvConfigView studyEnvContext={studyEnvContext} portalContext={portalContext}
      key={studyEnvContext.currentEnvPath}/>
    <PortalEnvConfigView portalEnv={portalEnv} portalContext={portalContext}
      key={portalEnv.environmentName}/>
  </div>
}

/** allows editing config settings for a particular study */
export function StudyEnvConfigView({ studyEnvContext, portalContext }:
                                       {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const [config, setConfig] = useState(studyEnvContext.currentEnv.studyEnvironmentConfig)
  const [isLoading, setIsLoading] = useState(false)
  const [kitTypeOptions, setKitTypeOptions] = useState<{ value: string, label: string }[]>([])
  const [selectedKitTypes, setSelectedKitTypes] = useState<{ value: string, label: string }[]>([])

  const { user } = useUser()

  const studyEnvParams = {
    portalShortcode: portalContext.portal.shortcode,
    studyShortcode: studyEnvContext.study.shortcode,
    envName: studyEnvContext.currentEnv.environmentName as EnvironmentName
  }

  const loadAllowedKitTypes = async () => {
    const allowedKitTypes = await Api.fetchAllowedKitTypes(studyEnvParams)
    const allowedKitTypeOptions = allowedKitTypes.map(kt => ({ value: kt.name, label: kt.displayName }))
    setKitTypeOptions(allowedKitTypeOptions)
  }

  const loadConfiguredKitTypes = async () => {
    const selectedKitTypes = await Api.fetchKitTypes(studyEnvParams)
    setSelectedKitTypes(selectedKitTypes.map(kt => ({ value: kt.name, label: kt.displayName })))
  }

  useEffect(() => {
    loadAllowedKitTypes()
    loadConfiguredKitTypes()
  }, [studyEnvContext.currentEnvPath])

  /** update a given field in the config */
  const updateConfig = (propName: keyof StudyEnvironmentConfig, value: string | boolean) => {
    setConfig(set(propName, value))
  }

  useUpdateEffect(() => {
    setConfig(studyEnvContext.currentEnv.studyEnvironmentConfig)
  }, [studyEnvContext.currentEnv.environmentName, studyEnvContext.study.shortcode])

  /** saves any changes to the server */
  const save = async () => {
    doApiLoad(async () => {
      await Api.updateStudyEnvironmentConfig(portalContext.portal.shortcode,
        studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName, config)
      Store.addNotification(successNotification('Config saved'))
      portalContext.reloadPortal(portalContext.portal.shortcode)
    }, { setIsLoading })
  }

  const saveKitTypes = async () => {
    doApiLoad(async () => {
      await Api.updateKitTypes(studyEnvParams, selectedKitTypes.map(k => k.value))
      Store.addNotification(successNotification('Kit types saved'))
      portalContext.reloadPortal(portalContext.portal.shortcode)
    }, { setIsLoading })
  }

  return <form className="bg-white mb-5" onSubmit={e => e.preventDefault()}>
    <h2 className="h4">{studyEnvContext.study.name} study configuration</h2>
    <p>Configure whether participants can access study content, such as surveys and consents.</p>
    <div>
      <label className="form-label">
        password protected <input type="checkbox" checked={config.passwordProtected}
          onChange={e => updateConfig('passwordProtected', e.target.checked)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        password <input type="text" className="form-control" value={config.password ?? ''}
          onChange={e => updateConfig('password', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label">
        accepting enrollment <InfoPopup content={`Uncheck this to hide the study from participants who have not
        yet joined and prevent any further enrollments.`}/>
        <input type="checkbox" checked={config.acceptingEnrollment} className="ms-2"
          onChange={e => updateConfig('acceptingEnrollment', e.target.checked)}/>
      </label>
    </div>

    <div>
      <label className="form-label">
                accepting proxy enrollment <InfoPopup content={
          <span>
            Enables enrolling as a proxy on behalf of a dependent.
            Requires extensive changes to your pre-enroll; see the
            <ZendeskLink doc={DocsKey.PROXY_ENROLLMENT}> proxy enrollment documentation </ZendeskLink>
            for more details.
          </span>}/>
        <input type="checkbox" checked={config.acceptingProxyEnrollment} className="ms-2"
          onChange={e => updateConfig('acceptingProxyEnrollment', e.target.checked)}/>
      </label>
    </div>
    {
      userHasPermission(user, studyEnvContext.portal.id, 'prototype') && (
        <div>
          <label className="form-label">
            enable family linkage <InfoPopup content={'If checked, allows participants to be grouped by families.'}/>
            <input type="checkbox" checked={config.enableFamilyLinkage} className="ms-2"
              onChange={e => updateConfig('enableFamilyLinkage', e.target.checked)}/>
          </label>
        </div>
      )
    }
    { user?.superuser &&
      <><div>
        <label className="form-label">
          use mock kit requests <InfoPopup content={
          `If checked, kit requests will be mocked for this environment, and not sent to any external services.`}/>
          <input type="checkbox" checked={config.useStubDsm}
            onChange={e => updateConfig('useStubDsm', e.target.checked)}/>
        </label>
      </div>
      <div>
        <label className="form-label">
              use kit request development realm
          <InfoPopup content={
              `If checked, kit requests will be sent to DSM, but to a development realm so they can be reviewed, but 
                  will not be shipped. To actually mail kits, this and the above field should be unchecked.`}/>
          <input type="checkbox" checked={config.useDevDsmRealm}
            onChange={e => updateConfig('useDevDsmRealm', e.target.checked)}/>
        </label>
      </div></>
    }

    <Button onClick={save}
      variant="primary" disabled={isLoading}
      tooltip={'Save'}>
      {isLoading && <LoadingSpinner/>}
      {!isLoading && 'Save study config'}
    </Button>

    <div>
      <h4 className="h5 mt-4">{studyEnvContext.study.name} kit type configuration</h4>
      <label className="form-label mb-0">
        kit types
      </label>
      <div style={{ width: 300 }}>
        <Select className="m-1" options={kitTypeOptions} isMulti={true} value={selectedKitTypes} isClearable={false}
          isDisabled={studyEnvContext.currentEnv.environmentName !== 'sandbox'}
          onChange={selected => setSelectedKitTypes(selected as { value: string, label: string }[])}
        />
      </div>
      {studyEnvContext.currentEnv.environmentName === 'sandbox' &&
          <Button onClick={saveKitTypes}
            variant="primary" disabled={isLoading}
            tooltip={'Save'}>
            {isLoading && <LoadingSpinner/>}
            {!isLoading && 'Save kit types'}
          </Button>}
    </div>
  </form>
}
