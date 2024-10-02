import React, { useState } from 'react'
import {
  StudyEnvironmentConfig,
  StudyEnvParams
} from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'
import Select from 'react-select'
import { Button } from 'components/forms/Button'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  doApiLoad,
  useLoadingEffect
} from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import {
  paramsFromContext,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import {
  InfoCard,
  InfoCardHeader,
  InfoCardTitle
} from 'components/InfoCard'
import { RequireUserPermission } from 'util/RequireUserPermission'

export const KitSettings = (
  {
    studyEnvContext,
    portalContext,
    config,
    updateConfig
  }: {
    studyEnvContext: StudyEnvContextT,
    portalContext: LoadedPortalContextT,
    config: StudyEnvironmentConfig,
    updateConfig: (key: keyof StudyEnvironmentConfig, value: unknown) => void
  }) => {
  const [isUpdatingKitTypes, setIsUpdatingKitTypes] = useState(false)
  const [kitTypeOptions, setKitTypeOptions] = useState<{ value: string, label: string }[]>([])
  const [selectedKitTypes, setSelectedKitTypes] = useState<{ value: string, label: string }[]>([])

  const studyEnvParams: StudyEnvParams = paramsFromContext(studyEnvContext)

  const { isLoading: isLoadingKitTypes } = useLoadingEffect(async () => {
    const [selectedKitTypes, allowedKitTypes] = await Promise.all([
      Api.fetchKitTypes(studyEnvParams),
      Api.fetchAllowedKitTypes(studyEnvParams)
    ])
    setSelectedKitTypes(selectedKitTypes.map(kt => ({ value: kt.name, label: kt.displayName })))
    const allowedKitTypeOptions = allowedKitTypes.map(kt => ({ value: kt.name, label: kt.displayName }))
    setKitTypeOptions(allowedKitTypeOptions)
  }, [studyEnvContext.currentEnvPath])

  const study = studyEnvContext.study
  const studyEnv = studyEnvContext.currentEnv

  const saveKitTypes = async () => {
    doApiLoad(async () => {
      await Api.updateKitTypes({
        portalShortcode: portalContext.portal!.shortcode,
        studyShortcode: study.shortcode,
        envName: studyEnv.environmentName
      }, selectedKitTypes.map(k => k.value))
      Store.addNotification(successNotification('Kit types saved'))
      portalContext.reloadPortal(portalContext.portal!.shortcode)
    }, { setIsLoading: setIsUpdatingKitTypes })
  }

  const isLoading = isLoadingKitTypes || isUpdatingKitTypes

  return <>
    <RequireUserPermission superuser>
      <InfoCard>
        <InfoCardHeader>
          <InfoCardTitle title={`Kit types`}/>
        </InfoCardHeader>
        <div className="p-2">
          <div style={{ width: 300 }} className="mb-2">
            <Select className="m-1" options={kitTypeOptions} isMulti={true} value={selectedKitTypes}
              isClearable={false}
              isDisabled={studyEnv.environmentName !== 'sandbox'}
              onChange={selected => setSelectedKitTypes(selected as {
                    value: string,
                    label: string
                  }[])}
            />
          </div>
          <Button onClick={saveKitTypes}
            variant="primary"
            disabled={isLoadingKitTypes || studyEnv.environmentName !== 'sandbox'}
            className={'mb-2'}
            tooltip={'Only possible in the sandbox environment'}>
            {isLoading && <LoadingSpinner/>}
            {!isLoading && 'Save kit types'}
          </Button>
        </div>

      </InfoCard>

      <div>
        <label className="form-label">
        Use mock kit requests <InfoPopup content={
          `If checked, kit requests will be mocked for this environment, `
          + `and not sent to any external services.`
          }/>
          <input type="checkbox" checked={config.useStubDsm}
            onChange={e => updateConfig('useStubDsm', e.target.checked)}/>
        </label>
      </div>
      <div>
        <label className="form-label">
        Use kit request development realm
          <InfoPopup content={
          `If checked, kit requests will be sent to DSM, but to a development realm so they can be reviewed, but 
               will not be shipped. To actually mail kits, this and the above field should be unchecked.`}/>
          <input type="checkbox" checked={config.useDevDsmRealm}
            onChange={e => updateConfig('useDevDsmRealm', e.target.checked)}/>
        </label>
      </div>
    </RequireUserPermission>
    <div>
      <label className="form-label">
        Enable in-person kits <InfoPopup content={
          `If checked, in-person kit requests will be enabled for this study environment.
          Participants will see information about completing in-person kits on the participant dashboard.`
        }/>
        <input type="checkbox" checked={config.enableInPersonKits}
          onChange={e => updateConfig('enableInPersonKits', e.target.checked)}/>
      </label>
    </div>
  </>
}
