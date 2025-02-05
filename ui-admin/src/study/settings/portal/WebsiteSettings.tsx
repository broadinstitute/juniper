import React from 'react'
import { PortalEnvironmentConfig } from '@juniper/ui-core'
import InfoPopup from 'components/forms/InfoPopup'

export const WebsiteSettings = (
  {
    config,
    updateConfig
  } : {
    config: PortalEnvironmentConfig,
    updateConfig: (key: keyof PortalEnvironmentConfig, value: unknown) => void
  }
) => {
  return <div>
    <p>Configure the accessibility of the landing page shown to all visitors, and sitewide properties</p>
    <div>
      <label className="form-label mt-2">
        password protected <input type="checkbox" checked={config.passwordProtected} className="ms-2"
          onChange={e => updateConfig('passwordProtected', e.target.checked)}/>
      </label>
    </div>
    <div>
      <label className="form-label mt-2">
        password <input type="text" className="form-control" value={config.password ?? ''}
          onChange={e => updateConfig('password', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label mt-2">
        accepting registration
        <input type="checkbox" checked={config.acceptingRegistration} className="ms-2"
          onChange={e => updateConfig('acceptingRegistration', e.target.checked)}/>
      </label>
    </div>
    <div>
      <label className="form-label mt-2">
        participant hostname
        <input type="text" className="form-control" value={config.participantHostname ?? ''}
          onChange={e => updateConfig('participantHostname', e.target.value)}/>
      </label>
    </div>
    <div>
      <label className="form-label mt-2">
        Mixpanel api token <InfoPopup content={<span>
        If provided, events for participant interactions with the website will
        be sent to the mixpanel project corresponding to the token</span>}/>
        <input type="text" className="form-control" value={config.mixpanelToken ?? ''}
          onChange={e => updateConfig('mixpanelToken', e.target.value)}/>
      </label>
    </div>
  </div>
}
