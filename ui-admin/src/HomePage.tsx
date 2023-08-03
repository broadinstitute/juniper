import React, { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Api, { Portal } from 'api/api'

import LoadingSpinner from 'util/LoadingSpinner'
import {portalParticipantsPath, studyParticipantsPath} from './portal/PortalRouter'
import {useNavContext} from "./navbar/NavContextProvider";

/** Shows a user the list of portals available to them */
function HomePage() {
  const [isLoading, setIsLoading] = useState(true)
  const navigate = useNavigate()
  const { portalList, setPortalList } = useNavContext()
  useEffect(() => {
    Api.getPortals().then(result => {
      setPortalList(result)
      if (result.length === 1) {
        navigate(portalParticipantsPath(result[0].shortcode, 'live'), { replace: true })
      } else {
        setIsLoading(false)
      }
    })
  }, [])
  return <div className="container">
    <h1 className="h2">Juniper Home</h1>
    <div className="ms-5 mt-4">
      <h2 className="h4">My Studies</h2>
      <LoadingSpinner isLoading={isLoading}>
        <ul className="list-group list-group-flush">
          { portalList.flatMap((portal, index) =>
              portal.portalStudies.map(portalStudy => {
                const study = portalStudy.study
                return <li key={`${portal.shortcode}-${study.shortcode}`} className="list-group-item">
                  <Link to={studyParticipantsPath(portal.shortcode, 'live', study.shortcode)}>
                    {study.name}
                  </Link>
                </li>
              })
          )}
        </ul>
      </LoadingSpinner>
    </div>
  </div>
}

export default HomePage
