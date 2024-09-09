import React from 'react'
import { PortalEnvironmentConfig } from '@juniper/ui-core'

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
  </div>
}
