import React, {
  useEffect,
  useState
} from 'react'
import {
  StudyEnvironmentConfig,
  StudyEnvParams
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
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import {
  InfoCard,
  InfoCardHeader,
  InfoCardTitle
} from 'components/InfoCard'

export const KitSettings = (
  {
    studyEnvContext,
    portalContext,
    config,
    updateConfig,
    canSave,
    saveConfig
  }: {
    studyEnvContext: StudyEnvContextT,
    portalContext: LoadedPortalContextT,
    config: StudyEnvironmentConfig,
    updateConfig: (key: keyof StudyEnvironmentConfig, value: unknown) => void,
    canSave: boolean,
    saveConfig: () => void
  }) => {
  const { user } = useUser()

  const studyEnvParams: StudyEnvParams = {
    portalShortcode: studyEnvContext.portal.shortcode,
    studyShortcode: studyEnvContext.study.shortcode,
    envName: studyEnvContext.currentEnv.environmentName
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


  const [isLoadingKitTypes, setIsLoadingKitTypes] = useState(false)
  const [kitTypeOptions, setKitTypeOptions] = useState<{ value: string, label: string }[]>([])
  const [selectedKitTypes, setSelectedKitTypes] = useState<{ value: string, label: string }[]>([])

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
    }, {  setIsLoading: setIsLoadingKitTypes })
  }

  return <div>
    {user?.superuser &&
        <>
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
                {isLoadingKitTypes && <LoadingSpinner/>}
                {!isLoadingKitTypes && 'Save kit types'}
              </Button>
            </div>

          </InfoCard>

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

          <Button variant="primary" onClick={saveConfig} disabled={!canSave}>Save study settings</Button>

        </>
    }
  </div>
}
