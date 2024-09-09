import React, { useState } from 'react'
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
import useUpdateEffect from 'util/useUpdateEffect'
import { useUser } from 'user/UserProvider'

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

  const { user } = useUser()

  const studyEnvParams = {
    portalShortcode: portalContext.portal.shortcode,
    studyShortcode: studyEnvContext.study.shortcode,
    envName: studyEnvContext.currentEnv.environmentName as EnvironmentName
  }


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

  return <form className="bg-white mb-5" onSubmit={e => e.preventDefault()}>


    <Button onClick={save}
      variant="primary" disabled={isLoading}
      tooltip={'Save'}>
      {isLoading && <LoadingSpinner/>}
      {!isLoading && 'Save study config'}
    </Button>


  </form>
}
