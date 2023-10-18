import React, { useState } from 'react'
import Api, { PortalEnvironment } from 'api/api'
import { useUser } from 'user/UserProvider'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Button } from 'components/forms/Button'
import { set } from 'lodash/fp'
import { LoadedPortalContextT } from './PortalProvider'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from '../util/LoadingSpinner'


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
  /** update a given field in the config */
  const updateConfig = (propName: string, value: string | boolean) => {
    setConfig(set(propName, value))
  }
  /** saves any changes to the server */
  const save = async (e: React.MouseEvent) => {
    e.preventDefault()
    doApiLoad(async () => {
      await Api.updatePortalEnvConfig(portal.shortcode, portalEnv.environmentName, config)
      Store.addNotification(successNotification('Portal config saved'))
      reloadPortal(portal.shortcode)
    }, { setIsLoading })
  }
  return <form className="bg-white p-3">
    <h2 className="h4">Website configuration ({portalContext.portal.name})</h2>
    <p>Configure the accessibility of the landing page shown to all visitors, and sitewide properties</p>
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
    <Button onClick={save}
      variant="primary" disabled={!user.superuser || isLoading}
      tooltip={user.superuser ? 'Save' : 'You do not have permission to edit these settings'}>
      {isLoading && <LoadingSpinner/>}
      {!isLoading && 'Save website config'}
    </Button>
  </form>
}

export default PortalEnvConfigView
