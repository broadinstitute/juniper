import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Api, { Portal } from 'api/api'

import LoadingSpinner from 'util/LoadingSpinner'

/** Shows a user the list of portals available to them */
function PortalList() {
  const [portalList, setPortalList] = useState<Portal[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()
  useEffect(() => {
    Api.getPortals().then(result => {
      if (result.length === 1) {
        navigate(`/${result[0].shortcode}`, { replace: true })
      } else {
        setPortalList(result)
        setIsLoading(false)
      }
    })
  }, [])
  return <div>
    <div className="App-study-list position-absolute top-50 start-50 translate-middle">
      <h4 className="text-center">Arbor</h4>

      <h6 className="mt-3">Select a portal</h6>
      <LoadingSpinner isLoading={isLoading}>
        <ul>
          { portalList.map((study, index) => <li key={index} className="mt-3">
            <Link to={`/${study.shortcode}`}>{study.name}</Link>
          </li>)}
        </ul>

      </LoadingSpinner>
    </div>
  </div>
}

export default PortalList
