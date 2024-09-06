import React, { useState } from 'react'
import {
  Study,
  StudyEnvironment,
  StudyEnvironmentConfig
} from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'
import { useUser } from 'user/UserProvider'
import Select from 'react-select'
import { Button } from 'components/forms/Button'
import LoadingSpinner from 'util/LoadingSpinner'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import { PortalContextT } from 'portal/PortalProvider'

export const StudyEnrollmentSettings = (
  {
    portalContext,
    studyEnv,
    study,
    config,
    updateConfig,
    saveConfig
  }: {
    portalContext: PortalContextT,
    studyEnv: StudyEnvironment,
    study: Study,
    config: StudyEnvironmentConfig,
    updateConfig: (key: keyof StudyEnvironmentConfig, value: unknown) => void,
    saveConfig: () => void
  }) => {
  const { user } = useUser()

  const [isLoadingKitTypes, setIsLoadingKitTypes] = useState(false)
  const [kitTypeOptions, setKitTypeOptions] = useState<{ value: string, label: string }[]>([])
  const [selectedKitTypes, setSelectedKitTypes] = useState<{ value: string, label: string }[]>([])


  const saveKitTypes = async () => {
    doApiLoad(async () => {
      await Api.updateKitTypes({
        portalShortcode: portalContext.portal!.shortcode,
        studyShortcode: study.shortcode,
        envName: studyEnv.environmentName
      }, selectedKitTypes.map(k => k.value))
      Store.addNotification(successNotification('Kit types saved'))
      portalContext.reloadPortal(portalContext.portal!.shortcode)
    }, {  setIsLoading: setIsLoadingKitTypes })
  }

  return <div>
    {user?.superuser &&
        <>
          <div>
            <label className="form-label">
                    use mock kit requests <InfoPopup content={
                      `If checked, kit requests will be mocked for this environment, `
                      + `and not sent to any external services.`
              }/>
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
          </div>

          <div>
            <h4 className="h5 mt-4">{study.name} kit type configuration</h4>
            <label className="form-label mb-0">
                    kit types
            </label>
            <div style={{ width: 300 }}>
              <Select className="m-1" options={kitTypeOptions} isMulti={true} value={selectedKitTypes}
                isClearable={false}
                isDisabled={studyEnv.environmentName !== 'sandbox'}
                onChange={selected => setSelectedKitTypes(selected as { value: string, label: string }[])}
              />
            </div>
            {studyEnv.environmentName === 'sandbox' &&
                  <Button onClick={saveKitTypes}
                    variant="primary" disabled={isLoadingKitTypes}
                    tooltip={'Save'}>
                    {isLoadingKitTypes && <LoadingSpinner/>}
                    {!isLoadingKitTypes && 'Save kit types'}
                  </Button>}
          </div>
        </>
    }
  </div>
}
