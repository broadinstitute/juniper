import React, { useEffect, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheckCircle, faExclamationCircle } from '@fortawesome/free-solid-svg-icons'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'

export const StatusPage = () => {
  const [operational, setOperational] = useState<boolean>()

  const loadSystemStatus = async () => {
    const systemStatus = await Api.getSystemStatus()
    setOperational(systemStatus.ok)
  }

  useEffect(() => {
    loadSystemStatus()
  }, [])

  return <div className="d-flex flex-column align-items-center justify-content-center vh-100">
    {operational === undefined ?
      <LoadingSpinner /> :
      <SystemStatus operational={operational}/>
    }
    <div className="text-center">
      <div className="text-muted">
        Please contact <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a> for additional support.
      </div>
    </div>
  </div>
}


const SystemStatus = ({ operational }: { operational: boolean }) => {
  return (
    <div className="card mb-3" style={{ maxWidth: '500px' }}>
      <div className="row g-0 align-items-center">
        <div className="col-md-4">
          <FontAwesomeIcon
            icon={operational ? faCheckCircle : faExclamationCircle}
            className={`text-center fa-8x p-4 ${operational ? 'text-success' : 'text-danger'}`}
          />
        </div>
        <div className="col-md-8">
          <div className="card-body">
            <h5 className="card-title">Juniper Status</h5>
            <p className="card-text">
              {operational ? (
                <>
                  Juniper is
                  <span className="fw-bold text-success"> operational </span>
                  and all systems are functioning normally
                </>
              ) : (
                <>
                  Juniper is currently experiencing
                  <span className="fw-bold text-danger"> degraded </span>
                  functionality. Users may experience issues with some or all functionality
                </>
              )}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
