import React, { useState } from 'react'
import { PortalEnvironment } from 'api/api'

/** shows a small banner in the top center indicating a non-live environment */
export default function EnvironmentAlert({ portalEnvironment }: { portalEnvironment: PortalEnvironment }) {
  const [isVisible, setIsVisible] = useState(portalEnvironment.environmentName !== 'PRODUCTION')
  const alertStyle = {
    background: 'rgba(200, 200, 200, 0.5)',
    color: 'black',
    padding: '0.5em',
    border: '1px solid #ccc',
    borderTop: 'none',
    borderBottomLeftRadius: '5px',
    borderBottomRightRadius: '5px',
    transform: 'rotateY(90deg)'
  }

  if (!isVisible) {
    return <></>
  }
  return <div className="position-absolute top-0 start-50 translate-middle-x" style={alertStyle}
    onClick={() => setIsVisible(false)}>
    {portalEnvironment.environmentName}
  </div>
}
