import React, { useContext } from 'react'
import { PortalContext } from 'providers/PortalProvider'

export default function PortalDashboard() {
  const portalContext = useContext(PortalContext)
  return <div>
    successfully loaded {portalContext.portal?.name}
  </div>
}
