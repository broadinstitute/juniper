import React from 'react'

import { Portal, Study } from 'api/api'
import { Link } from 'react-router-dom'
import PortalEnvConfigView from './PortalEnvConfigView'

/** Page an admin user sees immediately after logging in */
export default function PortalDashboard({ portal }: {portal: Portal}) {
  return <div className="p-4 container">
    <h2 className="h3">Environments</h2>
    <div className="row">
      <ul className="list-unstyled">
        { portal.portalEnvironments.map(portalEnv => <li key={portalEnv.environmentName}>
          <PortalEnvConfigView portalEnv={portalEnv} portalShortcode={portal.shortcode}/>
        </li>)}
      </ul>
    </div>
    <h2 className="h3">Studies</h2>
    <div className="row">
      <ul className="list-unstyled">
        { portal.portalStudies.map(portalStudy => {
          const study = portalStudy.study
          return <li key={study.shortcode}>
            <StudyConfigView study={study}/>
          </li>
        }
        )}
      </ul>
    </div>
  </div>
}

/** basic info about configuration for a given study */
function StudyConfigView({ study }: {study: Study}) {
  return <div className="bg-white p-3">
    <h3 className="h5">{study.name}</h3>
    <Link to={`studies/${study.shortcode}`}>Configure content</Link>
  </div>
}

