import React, { useContext, useEffect } from 'react'
import { LoadedPortalContextT, PortalContext } from 'portal/PortalProvider'
import { Portal } from 'api/api'
import { Link } from 'react-router-dom'
import { NavbarContext } from '../navbar/NavbarProvider'

/** Page an admin user sees immediately after logging in */
function PortalDashboard({ portal }: {portal: Portal}) {
  return <div className="p-4">
    <h4>{portal.name}</h4>
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
