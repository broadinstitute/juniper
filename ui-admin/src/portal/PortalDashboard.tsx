import React from 'react'

import { getMediaUrl, Portal } from 'api/api'
import { renderPageHeader } from 'util/pageUtils'
import { useUser } from 'user/UserProvider'
import { Button } from '../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowUpRightFromSquare, faPencil, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'
import { useAdminUserContext } from '../providers/AdminUserProvider'
import { studyParticipantsPath } from './PortalRouter'

/** Page an admin user sees immediately after logging in */
export default function PortalDashboard({ portal }: {portal: Portal}) {
  const { user } = useUser()
  const { users } = useAdminUserContext()

  return <div className="p-4 container">
    {renderPageHeader(`${portal.name} Dashboard`)}
    <div className="row py-3">
      <div className="col-6 me-3">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Studies
              <Button onClick={() => console.log('foo')}
                tooltip={'Create a new study'}
                variant="light" className="border">
                <FontAwesomeIcon icon={faPlus} className="fa-lg"/>
              </Button>
            </div>
          </div>
          <div className="card-body">
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
                </li>
              })}
            </ul>
          </div>
        </div>
      </div>
      <div className="col-5 ms-3">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Study Team
              <Button onClick={() => console.log('foo')}
                tooltip={'View all team members'}
                variant="light" className="border">
                <FontAwesomeIcon icon={faArrowUpRightFromSquare} className="fa-lg"/>
              </Button>
            </div>
          </div>
          <div className="card-body">
            { users.filter(u => !u.superuser).map(user => <div key={user.id}>{user.username}</div>) }
          </div>
        </div>
      </div>
    </div>
    <div className="row py-3">
      <div className="col-3">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Website
              <Button onClick={() => console.log('foo')}
                tooltip={'Edit website'}
                variant="light" className="border">
                <FontAwesomeIcon icon={faPencil} className="fa-lg"/>
              </Button>
            </div>
          </div>
          <div className="card-body">
            Your website here
          </div>
        </div>
      </div>
      <div className="col-3 me-3">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Dashboard
              <Button onClick={() => console.log('foo')}
                tooltip={'Edit dashboard'}
                variant="light" className="border">
                <FontAwesomeIcon icon={faPencil} className="fa-lg"/>
              </Button>
            </div>
          </div>
          <div className="card-body">
            Your dashboard here
          </div>
        </div>
      </div>
      <div className="col-5 ms-3">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Help & Tutorials
              <Link to="https://broad-juniper.zendesk.com/hc/en-us" target="_blank">
                <Button onClick={() => console.log('foo')}
                  tooltip={'View all help articles'}
                  variant="light" className="border">
                  <FontAwesomeIcon icon={faArrowUpRightFromSquare} className="fa-lg"/>
                </Button>
              </Link>
            </div>
          </div>
          <div className="card-body">
            Your help and tutorials here
          </div>
        </div>
      </div>
    </div>
    <div className="row py-3">
      <div className="col-6">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Study Trends
              <Button onClick={() => console.log('foo')}
                tooltip={'View all study trends'}
                variant="light" className="border">
                <FontAwesomeIcon icon={faArrowUpRightFromSquare} className="fa-lg"/>
              </Button>
            </div>
          </div>
          <div className="card-body">
            Your study trends here
          </div>
        </div>
      </div>
    </div>
  </div>
}
