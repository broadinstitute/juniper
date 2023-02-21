import React, { useContext } from 'react'

import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'
import { Portal, PortalEnvironment } from 'api/api'
import { Link } from 'react-router-dom'

/** Page an admin user sees immediately after logging in */
function PortalDashboard({ portal }: {portal: Portal}) {
  return <div className="p-4">
    <h4>{portal.name}</h4>
    <div className="p-5">
      <h5>Environments</h5>
      <div className="row">
        { portal.portalEnvironments.map(portalEnv => <PortalEnvConfigView
          portalEnv={portalEnv} key={portalEnv.environmentName}/>)}
      </div>
    </div>
    <div className="p-5"><h5>Website</h5></div>
    <div className="p-5">
      <h5>Studies</h5>
      <div>
        <ul className="list-group">
          { portal.portalStudies.map((portalStudy, index) => {
            const study = portalStudy.study
            return <li key={index} className="list-group-item">
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

function PortalEnvConfigView({ portalEnv }: {portalEnv: PortalEnvironment}) {
  const labelStyle = { minWidth: '15em', textAlign: 'left' as const }
  return <div className="col-md-4">
    <h6>{portalEnv.environmentName}
      <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>Edit</button>
    </h6>
    <div>
      <div>
        <label style={labelStyle}>Password protected:</label>
        {portalEnv.portalEnvironmentConfig.passwordProtected ? 'yes' : 'no'}
      </div>
      <div>
        <label style={labelStyle}>Password:</label>
        {portalEnv.portalEnvironmentConfig.password}
      </div>
      <div>
        <label style={labelStyle}>Accepting registration:</label>
        {portalEnv.portalEnvironmentConfig.acceptingRegistration ? 'yes' : 'no'}
      </div>
    </div>
  </div>
}

