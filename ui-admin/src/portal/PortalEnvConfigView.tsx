import React, { useState } from 'react'
import Api, { PortalEnvironment } from 'api/api'
import { useUser } from '../user/UserProvider'
import _cloneDeep from 'lodash/cloneDeep'
import { failureNotification, successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import { Portal } from '@juniper/ui-core/build/types/portal'

type PortalEnvConfigViewProps = {
  portal: Portal,
  portalEnv: PortalEnvironment,
  updatePortal: (portal: Portal) => void
}

/**
 * allows viewing/editing of portal environment configs, e.g. password protection
 */
const PortalEnvConfigView = ({ portal, portalEnv, updatePortal }: PortalEnvConfigViewProps) => {
  const [config, setConfig] = useState(portalEnv.portalEnvironmentConfig)
  const { user } = useUser()

  /** update a given field in the config */
  const updateConfig = (propName: string, value: string | boolean) => {
    const newConfig = _cloneDeep(config)
    // eslint-disable-next-line
    // @ts-ignore
    newConfig[propName] = value
    setConfig(newConfig)
  }
  // saves any changes to the server
  const save = async (e: React.MouseEvent) => {
    e.preventDefault()
    if (!user.superuser) {
      return
    }
    try {
      const updatedConfig = await Api.updatePortalEnvConfig(portal.shortcode, portalEnv.environmentName, config)
      Store.addNotification(successNotification('Config saved'))
      const updatedPortal = _cloneDeep(portal)
      const matchedEnv = updatedPortal.portalEnvironments
        .find(updatedPortalEnv => updatedPortalEnv.environmentName === portalEnv.environmentName) as PortalEnvironment
      matchedEnv.portalEnvironmentConfig = updatedConfig
      updatePortal(updatedPortal)
      setConfig(updatedConfig)
    } catch (e) {
      Store.addNotification(failureNotification(`Save failed ${  e}`))
    }
  }
  return <div className="container p-4">
    <h2 className="h5">Configuration settings - {portalEnv.environmentName}</h2>
    <form className="bg-white p-3">
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
          <input type="text" className="form-control" value={config.participantHostname}
            onChange={e => updateConfig('participantHostname', e.target.value)}/>
        </label>
      </div>
      <div>
        <label className="form-label">
        Email source address
          <input type="text" className="form-control" value={config.emailSourceAddress}
            onChange={e => updateConfig('emailSourceAddress', e.target.value)}/>
        </label>
      </div>
      <button onClick={save}
        aria-disabled={!user.superuser} className="btn btn-primary mt-3"
        title={user.superuser ? 'Save' : 'You do not have permission to edit these settings'}>
        Save
      </button>
    </form>
  </div>
}

export default PortalEnvConfigView
