import React, { useContext } from 'react'
import { PortalContext } from 'portal/PortalProvider'

/** Page an admin user sees immediately after logging in */
export default function PortalDashboard() {
  const portalContext = useContext(PortalContext)
  return <div>
    successfully loaded {portalContext.portal?.name}
  </div>
}
