import React from 'react'
import { Link } from 'react-router-dom'
import NotificationConfigTypeDisplay, { deliveryTypeDisplayMap } from './NotifcationConfigTypeDisplay'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'

export default function NotificationContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  return <div className="container m-3 p-3 bg-white">
    <h2 className="h4">Participant Notifications</h2>
    <div className="flex-grow-1 p-3">
      <ul className="list-unstyled">
        { currentEnv.notificationConfigs.map(config => <li key={config.id} className="p-1">
          <div className="d-flex">
            <Link to={`notificationConfigs/${config.id}`}>
              <NotificationConfigTypeDisplay config={config}/>
              <span className="detail"> ({deliveryTypeDisplayMap[config.deliveryType]})</span>
            </Link>
          </div>
        </li>
        ) }
      </ul>
      <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
        <FontAwesomeIcon icon={faPlus}/> Add
      </button>
    </div>
  </div>
}
