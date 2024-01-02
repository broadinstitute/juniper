import React, { useEffect, useState } from 'react'
import Api, { Enrollee, Event, Notification, NotificationConfig } from 'api/api'
import { notificationConfigPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from 'util/timeUtils'
import NotificationConfigTypeDisplay from '../../notifications/NotifcationConfigTypeDisplay'
import { Link } from 'react-router-dom'

const eventClassToHumanReadable = (eventClass: string): string => {
  return eventClass
    .split('_')
    .map(str => str.toLowerCase())
    .map(str => str[0].toUpperCase() + str.substring(1))
    .join(' ')
}

type EventNotificationTableEntry = {
  id: string,
  createdAt: number,
  deliveryStatus?: string,
  deliveryType?: string,
  notificationConfig?: NotificationConfig,
  notificationConfigId?: string,
  sentTo?: string,
  eventClass?: string,
}


/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function EnrolleeNotifications({ enrollee, studyEnvContext }:
                                                { enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal, currentEnvPath } = studyEnvContext
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [events, setEvents] = useState<Event[]>([])
  const [tableData, setTableData] = useState<EventNotificationTableEntry[]>([])
  const [isLoading, setIsLoading] = useState(true)

  /** matches each notification to a corresponding config by id */
  function attachConfigsToNotifications(rawNotifications: Notification[]) {
    rawNotifications.forEach(notification => {
      notification.notificationConfig = currentEnv.notificationConfigs
        .find(config => config.id === notification.notificationConfigId)
    })
  }

  const notificationToTableEntry = (val: Notification): EventNotificationTableEntry => {
    return {
      id: val.id,
      deliveryType: val.deliveryType,
      deliveryStatus: val.deliveryStatus,
      notificationConfig: val.notificationConfig,
      notificationConfigId: val.notificationConfigId,
      createdAt: val.createdAt
    }
  }

  const eventToTableEntry = (val: Event): EventNotificationTableEntry => {
    return {
      id: val.id,
      createdAt: val.createdAt,
      eventClass: val.eventClass
    }
  }

  useEffect(() => {
    Api.fetchEnrolleeNotifications(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode)
      .then(response => {
        attachConfigsToNotifications(response)
        setNotifications(response)
        Api.fetchEnrolleeEvents(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode)
          .then(response => {
            setEvents(response)
            setIsLoading(false)
          })
      })
  }, [enrollee.shortcode])

  useEffect(() => {
    const newTableData: EventNotificationTableEntry[] = []

    newTableData.push(...notifications.map(notificationToTableEntry))
    newTableData.push(...events.map(eventToTableEntry))

    const sorted = newTableData.sort((obj1, obj2) => {
      if (obj1.createdAt > obj2.createdAt) {
        return 1
      }

      if (obj1.createdAt < obj2.createdAt) {
        return -1
      }

      return 0
    })

    setTableData(sorted)
  }, [events, notifications])

  return <div>
    <h5>Notifications</h5>
    <LoadingSpinner isLoading={isLoading}>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>notification</th>
            <th>event</th>
            <th>method</th>
            <th>status</th>
            <th>time</th>
            <th>config</th>
          </tr>
        </thead>
        <tbody>
          {tableData.map(row => {
            const matchedConfig = currentEnv.notificationConfigs
              .find(cfg => cfg.id === row.notificationConfigId)
            return <tr key={row.id}>
              <td>
                {row.notificationConfig && <NotificationConfigTypeDisplay config={row.notificationConfig}/>}
                {row.eventClass && eventClassToHumanReadable(row.eventClass)}
              </td>

              <td>
                {row.deliveryType && row.deliveryType}
              </td>
              <td>
                {row.deliveryStatus && row.deliveryStatus}
              </td>
              <td>
                {instantToDefaultString(row.createdAt)}
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

