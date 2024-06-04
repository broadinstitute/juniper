import React, { useState } from 'react'

import { getMediaUrl, Portal, Study } from 'api/api'
import { renderPageHeader } from 'util/pageUtils'
import DeleteStudyModal from 'study/adminTasks/DeleteStudyModal'
import { useNavContext } from 'navbar/NavContextProvider'
import { faEllipsisH, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Button, IconButton } from 'components/forms/Button'
import { studyParticipantsPath } from './PortalRouter'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { useUser } from 'user/UserProvider'
import CreateNewStudyModal from 'study/CreateNewStudyModal'

/** Page an admin user sees immediately after logging in */
export default function PortalDashboard({ portal }: {portal: Portal}) {
  const { user } = useUser()
  const [showNewStudyModal, setShowNewStudyModal] = useState(false)
  return <div className="p-4 container">
    <div className="row">
      {renderPageHeader(portal.name)}
      <div className="d-flex mt-2">
        <div className="w-100 pe-5">
          <ul className="list-group list-group-flush fs-5 list-unstyled">
            <li key={portal.shortcode}>
              <span className={'mt-2'}>Studies</span>
              {portal.portalStudies.length === 0 &&
                  <ul className="list-group list-group-flush fst-italic my-1">
                    <li className="list-group-item my-1 border border-secondary-subtle text-muted rounded">
                      No studies
                    </li>
                  </ul>}
              <ul className="list-group list-group-flush ">
                {portal.portalStudies.sort((a, b) => a.study.name.localeCompare(b.study.name)).map(portalStudy => {
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
          </ul>
          {user?.superuser && <Button variant='secondary' onClick={() => setShowNewStudyModal(true)}>
            <FontAwesomeIcon icon={faPlus}/> Add a study
          </Button>}
        </div>
        {showNewStudyModal && <CreateNewStudyModal portal={portal} onDismiss={() => setShowNewStudyModal(false)}/>}
      </div>
    </div>
  </div>
}

/**
 * Dropdown menu for study options
 */
export function StudyOptions({ study, portal }: { study: Study, portal: Portal }) {
  const [showDeleteStudyModal, setShowDeleteStudyModal] = useState(false)
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
