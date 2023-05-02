import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Api, { Portal } from 'api/api'

import LoadingSpinner from 'util/LoadingSpinner'
import { portalParticipantsPath } from './PortalRouter'

/** Shows a user the list of portals available to them */
function PortalList() {
  const [portalList, setPortalList] = useState<Portal[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()
  useEffect(() => {
    Api.getPortals().then(result => {
      if (result.length === 1) {
        navigate(portalParticipantsPath(result[0].shortcode, 'live'), { replace: true })
      } else {
        setPortalList(result)
        setIsLoading(false)
      }
    })
  }, [])
  return <div>
    <div className="App-study-list position-absolute top-50 start-50 translate-middle">
      <h4 className="text-center">Juniper</h4>

      <h6 className="mt-3">Select a portal</h6>
      <LoadingSpinner isLoading={isLoading}>
        <ul>
          { portalList.map((portal, index) => <li key={index} className="mt-3">
            <Link to={portalParticipantsPath(portal.shortcode, 'live')}>{portal.name}</Link>
          </li>)}
        </ul>

      </LoadingSpinner>
    </div>
  </div>
}

export default PortalList
