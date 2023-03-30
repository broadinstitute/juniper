import React, { useState } from 'react'
import { PortalEnvironment } from 'api/api'

/** shows a small banner in the top center indicating a non-live environment */
export default function EnvironmentAlert({ portalEnvironment }: { portalEnvironment: PortalEnvironment }) {
  const [isVisible, setIsVisible] = useState(portalEnvironment.environmentName !== 'live')
  const alertStyle = {
    background: 'rgba(200, 200, 200, 0.5)',
    color: 'black',
    padding: '0.5em',
    border: '1px solid #ccc',
    borderTop: 'none',
    borderBottomLeftRadius: '5px',
    borderBottomRightRadius: '5px',
    transform: 'rotateY(90deg)',
    zIndex: 100
  }

  if (!isVisible) {
    return <></>
  }
  return <aside className="position-absolute top-0 start-50 translate-middle-x" style={alertStyle}
    onClick={() => setIsVisible(false)}>
    {portalEnvironment.environmentName}
  </aside>
}
