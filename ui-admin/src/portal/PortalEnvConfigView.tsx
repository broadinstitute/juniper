import React, { useState } from 'react'
import Api, { PortalEnvironment, PortalStudy } from 'api/api'
import { useUser } from 'user/UserProvider'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Button } from 'components/forms/Button'
import { set } from 'lodash/fp'
import { LoadedPortalContextT } from './PortalProvider'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from '../util/LoadingSpinner'
import useUpdateEffect from '../util/useUpdateEffect'
import useReactSingleSelect from '../util/react-select-utils'
import { PortalEnvironmentLanguage } from '@juniper/ui-core'
import Select from 'react-select'
import { usePortalLanguage } from './languages/usePortalLanguage'
import PortalEnvLanguageEditor from './languages/PortalEnvLanguageEditor'
import _cloneDeep from 'lodash/cloneDeep'
import InfoPopup from '../components/forms/InfoPopup'


type PortalEnvConfigViewProps = {
  portalContext: LoadedPortalContextT
  portalEnv: PortalEnvironment
}

/**
 * allows viewing/editing of portal environment configs, e.g. password protection
 */
const PortalEnvConfigView = ({ portalContext, portalEnv }: PortalEnvConfigViewProps) => {
  const [config, setConfig] = useState(portalEnv.portalEnvironmentConfig)
  const { user } = useUser()
  const { portal, reloadPortal } = portalContext
  const [isLoading, setIsLoading] = useState(false)
  const { defaultLanguage } = usePortalLanguage()
  const [selectedLanguage, setSelectedLanguage] = useState<PortalEnvironmentLanguage | undefined>(defaultLanguage)
  const [workingLanguages, setWorkingLanguages] =
    useState<PortalEnvironmentLanguage[]>(_cloneDeep(portalEnv.supportedLanguages))
  /** update a given field in the config */
  const updateConfig = (propName: string, value: string | boolean) => {
    setConfig(set(propName, value))
  }
  useUpdateEffect(() => {
    setConfig(portalEnv.portalEnvironmentConfig)
    setWorkingLanguages(_cloneDeep(portalEnv.supportedLanguages))
  }, [portalContext.portal.shortcode, portalEnv.environmentName])
  /** saves any changes to the server */
  const save = async (e: React.MouseEvent) => {
    e.preventDefault()
    doApiLoad(async () => {
      await Api.updatePortalEnvConfig(portal.shortcode, portalEnv.environmentName, config)
      Store.addNotification(successNotification('Portal config saved'))
      reloadPortal(portal.shortcode)
    }, { setIsLoading })
  }

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

  const {
    onChange: primaryStudyOnChange, options: primaryStudyOptions,
    selectedOption: selectedPrimaryStudyOption, selectInputId: selectPrimaryStudyInputId
  } =
    useReactSingleSelect(
      portal.portalStudies,
      (portalStudy: PortalStudy) =>
        ({ label: portalStudy.study.name, value: portalStudy }),
      (opt: PortalStudy | undefined) => setConfig({
        ...config,
        primaryStudy: opt?.study.shortcode
      }),
      portal.portalStudies.find(ps => ps.study.shortcode === config.primaryStudy)
    )

  const editableLanguages = portalEnv.environmentName === 'sandbox'

  return <div>
    <form className="bg-white">
      <h4>Website configuration ({portalContext.portal.name})</h4>
      <p>Configure the accessibility of the landing page shown to all visitors, and sitewide properties</p>
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
          accepting registration
          <input type="checkbox" checked={config.acceptingRegistration}
            onChange={e => updateConfig('acceptingRegistration', e.target.checked)}/>
        </label>
      </div>
      <div>
        <label className="form-label">
          participant hostname
          <input type="text" className="form-control" value={config.participantHostname ?? ''}
            onChange={e => updateConfig('participantHostname', e.target.value)}/>
        </label>
      </div>
      <div>
        <label className="form-label">
          Email source address
          <input type="text" className="form-control" value={config.emailSourceAddress ?? ''}
            onChange={e => updateConfig('emailSourceAddress', e.target.value)}/>
        </label>
      </div>
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
      { portal.portalStudies.length > 1 && <div className="mb-3">

        <label className="form-label" htmlFor={selectPrimaryStudyInputId}>
          Primary study</label> <InfoPopup content={'The study that portal registrants will be taken to by default'}/>
        <Select options={primaryStudyOptions} className="col-md-3"
          value={selectedPrimaryStudyOption} inputId={selectPrimaryStudyInputId}
          isDisabled={portalEnv.environmentName !== 'sandbox'} aria-label={'Select a study'}
          onChange={primaryStudyOnChange}/>
      </div> }
      <Button onClick={save}
        variant="primary" disabled={!user?.superuser || isLoading}
        tooltip={user?.superuser ? 'Save' : 'You do not have permission to edit these settings'}>
        {isLoading && <LoadingSpinner/>}
        {!isLoading && 'Save website config'}
      </Button>
    </form>
    <form className="mt-5">
      <div className="col-md-6">
        <h4 className="h5">
          Supported languages <InfoPopup content={'This list determines which languages appear in the dropdown' +
          'of the participant view.  It is up to you to create the content for that language.'}/>
        </h4>
        <PortalEnvLanguageEditor items={workingLanguages} setItems={setWorkingLanguages}
          readonly={!editableLanguages} key={`${portal.shortcode}-${portalEnv.environmentName}`}/>
      </div>
      { editableLanguages && <Button onClick={saveLanguages}
        variant="primary" disabled={!user?.superuser || isLoading}
        tooltip={user?.superuser ? 'Save' : 'You do not have permission to edit these settings'}>
        {isLoading && <LoadingSpinner/>}
        {!isLoading && 'Save languages'}
      </Button> }
    </form>
  </div>
}

export default PortalEnvConfigView
