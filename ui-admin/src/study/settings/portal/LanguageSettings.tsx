import React, { useState } from 'react'
import {
  PortalEnvironment,
  PortalEnvironmentConfig,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import { Button } from 'components/forms/Button'
import Select from 'react-select'
import LoadingSpinner from 'util/LoadingSpinner'
import InfoPopup from 'components/forms/InfoPopup'
import PortalEnvLanguageEditor from 'portal/languages/PortalEnvLanguageEditor'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import useReactSingleSelect from 'util/react-select-utils'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import _cloneDeep from 'lodash/cloneDeep'
import { usePortalLanguage } from 'portal/languages/usePortalLanguage'
import { useUser } from 'user/UserProvider'
import {
  InfoCard,
  InfoCardHeader,
  InfoCardTitle
} from 'components/InfoCard'

export const LanguageSettings = (
  {
    portalEnv,
    portalContext,
    updateConfig
  } : {
    portalContext: LoadedPortalContextT,
    portalEnv: PortalEnvironment,
    config: PortalEnvironmentConfig,
    updateConfig: (key: keyof PortalEnvironmentConfig, value: unknown) => void
  }
) => {
  const { portal, reloadPortal } = portalContext

  const { user } = useUser()

  const { defaultLanguage } = usePortalLanguage()
  const [selectedLanguage, setSelectedLanguage] = useState<PortalEnvironmentLanguage | undefined>(defaultLanguage)
  const [workingLanguages, setWorkingLanguages] =
    useState<PortalEnvironmentLanguage[]>(_cloneDeep(portalEnv.supportedLanguages))
  const [isLoading, setIsLoading] = useState(false)

  const saveLanguages = async (e: React.MouseEvent) => {
    e.preventDefault()
    doApiLoad(async () => {
      await Api.setPortalEnvLanguages(portal.shortcode, portalEnv.environmentName, workingLanguages)
      Store.addNotification(successNotification('Portal languages saved'))
      reloadPortal(portal.shortcode)
    }, { setIsLoading })
  }

  const {
    onChange: languageOnChange, options: languageOptions,
    selectedOption: selectedLanguageOption, selectInputId: selectLanguageInputId
  } =
    useReactSingleSelect(
      portalEnv.supportedLanguages,
      (language: PortalEnvironmentLanguage) => ({ label: language.languageName, value: language }),
      setSelectedLanguage,
      selectedLanguage
    )
  const editableLanguages = portalEnv.environmentName === 'sandbox'

  return <>
    <InfoCard>
      <InfoCardHeader>
        <InfoCardTitle title={'Supported languages'}/>
        <InfoPopup content={'This list determines which languages appear in the dropdown' +
          'of the participant view.  It is up to you to create the content for that language.'}/>
      </InfoCardHeader>
      <div className="p-2">
        <PortalEnvLanguageEditor items={workingLanguages} setItems={setWorkingLanguages}
          readonly={!editableLanguages}
          key={`${portal.shortcode}-${portalEnv.environmentName}`}/>
        {editableLanguages && <Button
          onClick={saveLanguages}
          className="ms-3"
          variant="primary" disabled={!user?.superuser || isLoading}
          tooltip={user?.superuser ? 'Save' : 'You do not have permission to edit these settings'}>
          {isLoading && <LoadingSpinner/>}
          {!isLoading && 'Save languages'}
        </Button>}
      </div>
    </InfoCard>

    <div>
      <label className="form-label">
        Default portal language
        <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
          isDisabled={portalEnv.supportedLanguages.length < 2} aria-label={'Select a language'}
          onChange={e => {
            e && updateConfig('defaultLanguage', e.value.languageCode)
            languageOnChange(e)
          }}/>
      </label>
    </div>
  </>
}
