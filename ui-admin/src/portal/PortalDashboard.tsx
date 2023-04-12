import React from 'react'

import { Portal } from 'api/api'
import PortalEnvView from './PortalEnvView'
const ENV_SORT_ORDER = ['sandbox', 'irb', 'live']
/** Page an admin user sees immediately after logging in */
export default function PortalDashboard({ portal }: {portal: Portal}) {
  const sortedEnvs = portal.portalEnvironments.sort((pa, pb) =>
    ENV_SORT_ORDER.indexOf(pa.environmentName) - ENV_SORT_ORDER.indexOf(pb.environmentName))
  return <div className="p-4 container">
    <div className="row">
      <ul className="list-unstyled">
        { sortedEnvs.map(portalEnv => <li key={portalEnv.environmentName}>
          <PortalEnvView portalEnv={portalEnv} portal={portal}/>
        </li>)}
      </ul>
    </div>
  </div>
}
