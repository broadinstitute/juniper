import React from 'react'
import { PortalEnvironment } from '../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEdit } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'
import { mailingListPath } from './PortalRouter'

export default function PortalEnvConfigView({ portalShortcode, portalEnv }:
  {portalShortcode: string, portalEnv: PortalEnvironment}) {
  const labelStyle = { minWidth: '15em', textAlign: 'left' as const }
  const config = portalEnv.portalEnvironmentConfig
  return <div className="bg-white p-3">
    <h3 className="h5">{portalEnv.environmentName}
      <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
        <FontAwesomeIcon icon={faEdit}/>
      </button>
    </h3>

    <div className="ms-4">
      <h4 className="h6">Configuration</h4>
      <label style={labelStyle}>Password protected:
        <input className="ms-2" type={'checkbox'} readOnly={true} checked={config.passwordProtected}/>
      </label>
      {config.passwordProtected && <label className="ms-4">
        Password: <input type="text" readOnly={true} value={config.password}/>
      </label>}
      <div>
        <label style={labelStyle}>Accepting registration:
          <input className="ms-2" type={'checkbox'} readOnly={true} checked={config.acceptingRegistration}/>
        </label>
      </div>
      <div className="mt-4"><h4 className="h6">Website</h4></div>

      <div className="mt-4">
        <h4 className="h6">
          <Link to={mailingListPath(portalShortcode, portalEnv.environmentName)}>Mailing List</Link>
        </h4>
      </div>
    </div>
  </div>
}
