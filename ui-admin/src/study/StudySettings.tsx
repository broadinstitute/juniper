import React, { useState } from 'react'
import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { LoadedPortalContextT } from '../portal/PortalProvider'
import PortalEnvConfigView from '../portal/PortalEnvConfigView'
import { PortalEnvironment } from '@juniper/ui-core'
import { doApiLoad } from 'api/api-utils'
import { Button } from 'components/forms/Button'
import { set } from 'lodash/fp'
import { useUser } from 'user/UserProvider'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import LoadingSpinner from 'util/LoadingSpinner'

/** shows settings for both a study and its containing portal */
export default function StudySettings({ studyEnvContext, portalContext }:
{studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const portalEnv = portalContext.portal.portalEnvironments
    .find(env =>
      env.environmentName === studyEnvContext.currentEnv.environmentName) as PortalEnvironment
  return <div className="ps-4">
    <StudyEnvConfigView studyEnvContext={studyEnvContext} portalContext={portalContext}/>
    <PortalEnvConfigView portalEnv={portalEnv} portalContext={portalContext}/>
  </div>
}

/** allows editing config settings for a particular study */
export function StudyEnvConfigView({ studyEnvContext, portalContext }:
                                       {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const [config, setConfig] = useState(studyEnvContext.currentEnv.studyEnvironmentConfig)
  const { user } = useUser()
  const [isLoading, setIsLoading] = useState(false)
  /** update a given field in the config */
  const updateConfig = (propName: string, value: string | boolean) => {
    setConfig(set(propName, value))
  }

  /** saves any changes to the server */
  const save = async (e: React.MouseEvent) => {
    e.preventDefault()
    doApiLoad(async () => {
      await Api.updateStudyEnvironmentConfig(portalContext.portal.shortcode,
        studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName, config)
      Store.addNotification(successNotification('Config saved'))
      portalContext.reloadPortal(portalContext.portal.shortcode)
    }, { setIsLoading })
  }

  return <form className="bg-white p-3 mb-5">
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
        password <input type="text" className="form-control" value={config.password}
          onChange={e => updateConfig('password', e.target.value)}/>
      </label>
    </div>

    <Button onClick={save}
      variant="primary" disabled={!user.superuser || isLoading}
      tooltip={user.superuser ? 'Save' : 'You do not have permission to edit these settings'}>
      {isLoading && <LoadingSpinner/>}
      {!isLoading && 'Save study config'}
    </Button>
  </form>
}
