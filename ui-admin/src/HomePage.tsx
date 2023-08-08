import React from 'react'
import { Link } from 'react-router-dom'
import { studyParticipantsPath } from './portal/PortalRouter'
import { useNavContext } from './navbar/NavContextProvider'
import { getImageUrl } from './api/api'

/** Shows a user the list of portals available to them */
function HomePage() {
  const { portalList } = useNavContext()

  return <div className="container">
    <h1 className="h2">Juniper Home</h1>
    <div className="ms-5 mt-4">
      <h2 className="h4">My Studies</h2>
      <ul className="list-group list-group-flush fs-5">
        { portalList.flatMap(portal =>
          portal.portalStudies.map(portalStudy => {
            const study = portalStudy.study
            return <li key={`${portal.shortcode}-${study.shortcode}`}
              className="list-group-item my-1 border border-secondary-subtle rounded ">
              <Link to={studyParticipantsPath(portal.shortcode, study.shortcode, 'live')}>
                <img
                  src={getImageUrl(portal.shortcode, 'favicon.ico', 1)}
                  className="me-3" style={{ maxHeight: '1.5em' }}/>
                {study.name}
              </Link>
            </li>
          })
        )}
      </ul>
    </div>
  </div>
}

export default HomePage
