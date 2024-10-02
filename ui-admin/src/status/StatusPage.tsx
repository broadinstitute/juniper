import React, { useEffect } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheckCircle, faExclamationCircle } from '@fortawesome/free-solid-svg-icons'

export const StatusPage = () => {
  useEffect(() => {

  }, [])


  return <div className="d-flex flex-column align-items-center justify-content-center vh-100">
    <h3 className="mb-3">Juniper Status</h3>
    <SystemStatus title="Admin" operational={true}/>
    <SystemStatus title="Participant" operational={false}/>
    <div className="text-center">
      <div className="text-muted">
            Please contact <a href="mailto:support@juniper.terra.bio">
          support@juniper.terra.bio
        </a> for additional support.
      </div>
    </div>
  </div>
}


const SystemStatus = ({ title, operational }: {
    title: string,
    operational: boolean
}) => {
  if (!operational) {
    return <div className="card mb-3" style={{ maxWidth: '500px' }}>
      <div className="row g-0 align-items-center">
        <div className="col-md-4">
          <FontAwesomeIcon icon={faExclamationCircle} className={'text-center fa-8x text-danger p-4'}/>
        </div>
        <div className="col-md-8">
          <div className="card-body">
            <h5 className="card-title">Juniper {title} System</h5>
            <p className="card-text">
              Participants may experience
              <span className="fw-bold text-danger"> degraded </span>
              functionality while accessing their studies
            </p>
          </div>
        </div>
      </div>
    </div>
  }


  return <div className="card mb-3" style={{ maxWidth: '500px' }}>
    <div className="row g-0 align-items-center">
      <div className="col-md-4">
        <FontAwesomeIcon icon={faCheckCircle} className={'text-center fa-8x text-success p-4'}/>
      </div>
      <div className="col-md-8">
        <div className="card-body">
          <h5 className="card-title">Juniper {title} System</h5>
          <p className="card-text">
            This system is
            <span className="fw-bold text-success"> operational </span>
            and all systems are functioning normally
          </p>
        </div>
      </div>
    </div>
  </div>
}
