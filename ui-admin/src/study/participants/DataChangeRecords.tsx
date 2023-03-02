import React, { useEffect, useState } from 'react'
import Api, { DataChangeRecord, Enrollee } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from 'util/timeUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'

/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function DataChangeRecords({ enrollee, studyEnvContext }:
                                                {enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
  const [notifications, setNotifications] = useState<DataChangeRecord[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    Api.fetchEnrolleeChangeRecords(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode)
      .then(response => {
        setNotifications(response)
        setIsLoading(false)
      })
  }, [enrollee.shortcode])
  return <div>
    <h5>Notifications</h5>
    <LoadingSpinner isLoading={isLoading}>
      <table className="table table-striped">
        <thead >
          <tr>
            <th>time</th>
            <th>model</th>
            <th>update</th>
            <th>source</th>
          </tr>
        </thead>
        <tbody>
          {notifications.map(changeRecord => <tr key={changeRecord.id}>
            <td>
              {instantToDefaultString(changeRecord.createdAt)}
            </td>
            <td>
              {changeRecord.modelName}
            </td>
            <td>
              {changeRecord.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {changeRecord.newValue}
            </td>
            <td>
              {changeRecord.responsibleUserId ? 'Participant' : 'Admin'}
            </td>
          </tr>)}
        </tbody>
      </table>

    </LoadingSpinner>
  </div>
}

