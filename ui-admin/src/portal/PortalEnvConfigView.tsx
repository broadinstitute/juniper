import React from 'react'
import { PortalEnvironment } from 'api/api'

const PortalEnvConfigView = ({ portalEnv }: {portalEnv: PortalEnvironment}) => {
  const config = portalEnv.portalEnvironmentConfig

  return <div className="container p-4">
    <h2 className="h5">Configuration settings - {portalEnv.environmentName}</h2>
    <form className="bg-white p-3">
      <div>
        <label className="form-label">
        password protected <input type="checkbox" readOnly checked={config.passwordProtected}/>
        </label>
      </div>
      <div>
        <label className="form-label">
        password <input type="text" className="form-control" readOnly={true} value={config.password}/>
        </label>
      </div>
      <div>
        <label className="form-label">
        accepting registration <input type="checkbox" readOnly checked={config.acceptingRegistration}/>
        </label>
      </div>
      <div>
        <label className="form-label">
        participant hostname
          <input type="text" className="form-control" readOnly={true} value={config.participantHostname}/>
        </label>
      </div>
      <div>
        <label className="form-label">
        Email source address
          <input type="text" className="form-control" readOnly={true} value={config.emailSourceAddress}/>
        </label>
      </div>
    </form>
  </div>
}

export default PortalEnvConfigView
