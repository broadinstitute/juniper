import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { studyParticipantsPath } from './portal/PortalRouter'
import { useNavContext } from './navbar/NavContextProvider'
import { getMediaUrl } from './api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEllipsisH, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Button, IconButton } from './components/forms/Button'
import CreateNewStudyModal from './study/CreateNewStudyModal'
import { useUser } from './user/UserProvider'
import DeleteStudyModal from './study/adminTasks/DeleteStudyModal'
import { Study } from '@juniper/ui-core/build/types/study'
import CreateNewCohortModal from './study/CreateNewCohortModal'
import { Portal } from '@juniper/ui-core/build/types/portal'
import RenamePortalModal from './portal/RenamePortalModal'
import RenameStudyModal from './study/RenameStudyModal'

/** Shows a user the list of portals available to them */
function HomePage() {
  const { portalList } = useNavContext()
  const { user } = useUser()
  const [showNewStudyModal, setShowNewStudyModal] = useState(false)
  const [showNewCohortModal, setShowNewCohortModal] = useState(false)

  return <div className="container">
    <h1 className="h2">Juniper Home</h1>
    <div className="mt-4">
      <h2 className="h4">My Portals</h2>
      <ul className="list-group list-group-flush fs-5 list-unstyled">
        { portalList.sort((a, b) => a.name.localeCompare(b.name)).flatMap(portal =>
          <li key = {portal.shortcode} className="mt-3">
            <span className={'mt-2'}>{portal.name}</span>
            <PortalOptions portal={portal}/>
            { portal.portalStudies.length === 0 &&
                <ul className="list-group list-group-flush fst-italic my-1">
                  <li className="list-group-item my-1 border border-secondary-subtle text-muted rounded">
                    No studies</li></ul>}
            <ul className="list-group list-group-flush ">
              { portal.portalStudies.sort((a, b) => a.study.name.localeCompare(b.study.name)).map(portalStudy => {
                const study = portalStudy.study
                return <li key={`${portal.shortcode}-${study.shortcode}`}
                  className="list-group-item my-1 border border-secondary-subtle rounded">
                  <Link to={studyParticipantsPath(portal.shortcode, study.shortcode, 'live')}>
                    <img
                      src={getMediaUrl(portal.shortcode, 'favicon.ico', 'latest')}
                      className="me-3" style={{ maxHeight: '1.5em' }} alt={study.name}/>
                    {study.name}
                  </Link>
                  <StudyOptions study={study} portal={portal}/>
                </li>
              })}
            </ul>
          </li>
        )}
      </ul>
      {user?.superuser && <Button variant='secondary' onClick={() => setShowNewStudyModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Add a study
      </Button>}
      {user?.superuser && <Button variant='secondary' onClick={() => setShowNewCohortModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Create a cohort
      </Button>}
    </div>
    {showNewStudyModal && <CreateNewStudyModal onDismiss={() => setShowNewStudyModal(false)}/>}
    {showNewCohortModal && <CreateNewCohortModal onDismiss={() => setShowNewCohortModal(false)}/>}
  </div>
}

/**
 *
 */
export function StudyOptions({ study, portal }: { study: Study, portal: Portal }) {
  const [showDeleteStudyModal, setShowDeleteStudyModal] = useState(false)
  const [showRenameStudyModal, setShowRenameStudyModal] = useState(false)
  const [selectedStudy, setSelectedStudy] = useState<Study>()
  const [selectedPortal, setSelectedPortal] = useState<Portal>()
  const { reload } = useNavContext()

  return <span className="dropdown">
    <span className="nav-item dropdown ms-1">
      <IconButton icon={faEllipsisH} data-bs-toggle="dropdown"
        aria-expanded="false" aria-label="Configure Study"/>
      <div className="dropdown-menu">
        <ul className="list-unstyled">
          <li>
            <button className="dropdown-item"
              onClick={
                () => {
                  setShowRenameStudyModal(!showDeleteStudyModal)
                  setSelectedStudy(study)
                  setSelectedPortal(portal)
                }}>Rename
            </button>
            {selectedStudy && showRenameStudyModal && selectedPortal &&
              <RenameStudyModal study={selectedStudy}
                portal={selectedPortal}
                onClose={() => {
                  reload()
                  setShowRenameStudyModal(false)
                }}/>
            }
          </li>
          <li>
            <button className="dropdown-item"
              onClick={
                () => {
                  setShowDeleteStudyModal(!showDeleteStudyModal)
                  setSelectedStudy(study)
                  setSelectedPortal(portal)
                }}>Delete
            </button>
            {selectedStudy && showDeleteStudyModal && selectedPortal &&
                <DeleteStudyModal study={selectedStudy}
                  portal={selectedPortal}
                  reload={reload}
                  onDismiss={() => setShowDeleteStudyModal(false)}/>
            }
          </li>
        </ul>
      </div>
    </span>
  </span>
}

/**
 *
 */
export function PortalOptions({ portal }: { portal: Portal }) {
  const [showRenamePortalModal, setShowRenamePortalModal] = useState(false)
  const { reload } = useNavContext()

  return <span className="dropdown">
    <span className="nav-item dropdown">
      <IconButton icon={faEllipsisH} data-bs-toggle="dropdown"
        aria-expanded="false" aria-label="Configure Portal"/>
      <div className="dropdown-menu">
        <ul className="list-unstyled">
          <li>
            <button className="dropdown-item"
              onClick={
                () => {
                  setShowRenamePortalModal(!showRenamePortalModal)
                }}>Rename
            </button>
            {showRenamePortalModal &&
              <RenamePortalModal portal={portal}
                onClose={() => {
                  reload()
                  setShowRenamePortalModal(false)
                }}/>
            }
          </li>
        </ul>
      </div>
    </span>
  </span>
}

export default HomePage
