import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { studyParticipantsPath } from './portal/PortalRouter'
import { useNavContext } from './navbar/NavContextProvider'
import { getImageUrl } from './api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { Button } from './components/forms/Button'
import CreateNewStudyModal from './study/CreateNewStudyModal'
import { useUser } from './user/UserProvider'

/** Shows a user the list of portals available to them */
function HomePage() {
  const { portalList } = useNavContext()
  const { user } = useUser()
  const [showNewStudyModal, setShowNewStudyModal] = useState(false)

  return <div className="container">
    <h1 className="h2">Juniper Home</h1>
    <div className="ms-5 mt-4">
      <h2 className="h4">My Studies</h2>
      <ul className="list-group list-group-flush fs-5">
        { portalList.sort((a, b) => a.name.localeCompare(b.name)).flatMap(portal =>
          portal.portalStudies.sort((a, b) => a.study.name.localeCompare(b.study.name)).map(portalStudy => {
            const study = portalStudy.study
            return <li key={`${portal.shortcode}-${study.shortcode}`}
              className="list-group-item my-1 border border-secondary-subtle rounded">
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
      { user.superuser && <Button variant='secondary' onClick={() => setShowNewStudyModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Add a study
      </Button> }
    </div>
    { showNewStudyModal && <CreateNewStudyModal onDismiss={() => setShowNewStudyModal(false)}/> }
  </div>
}

export default HomePage
