import React, { useEffect, useState } from 'react'
import Api, { Enrollee, NotificationConfig, Notification } from '../../api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import LoadingSpinner from '../../util/LoadingSpinner'
import { instantToDefaultString } from '../../util/timeUtils'

/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function EnrolleeNotifications({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal } = studyEnvContext
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [isLoading, setIsLoading] = useState(true)

  /** matches each notification to a corresponding config by id */
  function attachConfigsToNotifications(rawNotifications: Notification[]) {
    rawNotifications.forEach(notification => {
      notification.notificationConfig = currentEnv.notificationConfigs
        .find(config => config.id === notification.notificationConfigId)
    })
  }

  useEffect(() => {
    Api.fetchEnrolleeNotifications(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode)
      .then(response => {
        attachConfigsToNotifications(response)
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
            <th>notification</th>
            <th>method</th>
            <th>status</th>
            <th>time</th>
          </tr>
        </thead>
        <tbody>
          {notifications.map(notification => <tr key={notification.id}>
            <td>
              <NotificationConfigTypeDisplay config={notification.notificationConfig}/>
            </td>
            <td>
              {notification.deliveryType}
            </td>
            <td>
              {notification.deliveryStatus}
            </td>
            <td>
              {instantToDefaultString(notification.createdAt)}
            </td>
          </tr>)}
        </tbody>
      </table>

    </LoadingSpinner>
  </div>
}

/** shows a summary of the notification config */
function NotificationConfigTypeDisplay({ config }: {config?: NotificationConfig}) {
  if (!config) {
    return <></>
  }
  if (config.notificationType === 'EVENT') {
    return <span>{config.eventType}</span>
  } else {
    return <span>{config.taskType} - {config.taskTargetStableId}</span>
  }
}
