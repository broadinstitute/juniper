import React, { useContext } from 'react'

import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'
import { Portal, PortalEnvironment } from 'api/api'
import { Link } from 'react-router-dom'

/** Page an admin user sees immediately after logging in */
function PortalDashboard({ portal }: {portal: Portal}) {
  return <div className="p-4">
    <h4>{portal.name}</h4>
    <div className="p-5">
      <h5>Portal Environments</h5>
      <div className="row">
        { portal.portalEnvironments.map(portalEnv => <PortalEnvConfigView
          portalEnv={portalEnv} key={portalEnv.environmentName}/>)}
      </div>
    </div>
    <div className="p-5">
      <h5>Studies</h5>
      <div>
        <ul className="list-unstyled">
          { portal.portalStudies.map((portalStudy, index) => {
            const study = portalStudy.study
            return <li key={index} className="bg-white p-2">
              <h6>{portalStudy.study.name}</h6>
              <Link to={`studies/${study.shortcode}`}>Configure content</Link>
            </li>
          }
          )}
        </ul>
      </div>
    </div>
  </div>
}

/** Reads the portal object to show in the dashboard from context */
export default function PortalDashboardFromContext() {
  const portalContext = useContext(PortalContext) as LoadedPortalContextT
  return <PortalDashboard portal={portalContext.portal as Portal}/>
}

/** show the config settings for a given environment */
function PortalEnvConfigView({ portalEnv }: {portalEnv: PortalEnvironment}) {
  const inputStyle = {
    display: 'inline-block',
    maxWidth: '14em',
    width: '14em'
  }
  return <div className="col-md-12 bg-white my-1">
    <h3 className="h5">
      <button className="btn btn-secondary fw-bold" onClick={() => alert('not yet implemented')}>
        {portalEnv.environmentName}
      </button>
    </h3>
    <div className="ps-4">
      <div>
        <label className="form-label">Password protected:
          <input className="ms-2" type="checkbox" disabled={true}
            checked={portalEnv.portalEnvironmentConfig.passwordProtected}/>
        </label>
      </div>
      <div>
        <label className="form-label">Password:
          <input className="form-control ms-2" style={inputStyle} type="text"
            readOnly={true} value={portalEnv.portalEnvironmentConfig.password}/>
        </label>
      </div>
      <div>
        <label className="form-label">Accepting registration:
          <input className="ms-2" type="checkbox" disabled={true}
            checked={portalEnv.portalEnvironmentConfig.acceptingRegistration}/>
        </label>
      </div>
    </div>
  </div>
}

