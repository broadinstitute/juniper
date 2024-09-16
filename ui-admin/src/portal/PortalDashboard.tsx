import React from 'react'

import { Portal } from 'api/api'
import { renderPageHeader } from 'util/pageUtils'
import { useUser } from 'user/UserProvider'
import { Button } from '../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowUpRightFromSquare, faPencil, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Link } from 'react-router-dom'

/** Page an admin user sees immediately after logging in */
export default function PortalDashboard({ portal }: {portal: Portal}) {
  const { user } = useUser()
  return <div className="p-4 container">
    {renderPageHeader(`${portal.name} Dashboard`)}
    <div className="row py-3">
      <div className="col-6 border me-3">
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
            Your studies here
          </div>
        </div>
      </div>
      <div className="col-4 border ms-3">
        <div className="card">
          <div className="card-header">
            <div className="d-flex align-items-center justify-content-between w-100">
              Manage Team
              <Button onClick={() => console.log('foo')}
                tooltip={'View all team members'}
                variant="light" className="border">
                <FontAwesomeIcon icon={faArrowUpRightFromSquare} className="fa-lg"/>
              </Button>
            </div>
          </div>
          <div className="card-body">
            Your teammates here
          </div>
        </div>
      </div>
    </div>
    <div className="row py-3">
      <div className="col-3 border">
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
      <div className="col-3 border me-3">
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
      <div className="col-4 border ms-3">
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
      <div className="col-6 border">
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
