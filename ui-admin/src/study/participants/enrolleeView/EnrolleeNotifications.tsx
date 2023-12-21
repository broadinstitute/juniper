import React, { useEffect, useState } from 'react'
import Api, { Enrollee, Event, Notification } from 'api/api'
import { notificationConfigPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from 'util/timeUtils'
import NotificationConfigTypeDisplay from '../../notifications/NotifcationConfigTypeDisplay'
import { Link } from 'react-router-dom'

/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function EnrolleeNotifications({ enrollee, studyEnvContext }:
                                                  { enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal, currentEnvPath } = studyEnvContext
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [events, setEvents] = useState<Event[]>([])
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
        Api.fetchEnrolleeEvents(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode)
          .then(response => {
            setEvents(response)
            console.log(events)
            setIsLoading(false)
          })
      })
  }, [enrollee.shortcode])
  return <div>
    <h5>Notifications</h5>
    <LoadingSpinner isLoading={isLoading}>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>notification</th>
            <th>method</th>
            <th>status</th>
            <th>time</th>
            <th>config</th>
          </tr>
        </thead>
        <tbody>
          {notifications.map(notification => {
            const matchedConfig = currentEnv.notificationConfigs
              .find(cfg => cfg.id === notification.notificationConfigId)
            return <tr key={notification.id}>
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
              <td>
                {matchedConfig &&
                                <Link to={notificationConfigPath(matchedConfig, currentEnvPath)}>config</Link>}
              </td>
            </tr>
          })}
        </tbody>
      </table>

    </LoadingSpinner>
  </div>
}

