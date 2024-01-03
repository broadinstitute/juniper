import React, { useEffect, useState } from 'react'
import Api, { Enrollee, Notification, StudyEnvironment } from 'api/api'
import { StudyEnvContextT, notificationConfigPath } from '../../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from 'util/timeUtils'
import NotificationConfigTypeDisplay from '../../notifications/NotifcationConfigTypeDisplay'
import { Link } from 'react-router-dom'
import { ColumnDef, getCoreRowModel, getSortedRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout, renderEmptyMessage } from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEnvelope, faEnvelopeOpen } from '@fortawesome/free-solid-svg-icons'

const notificationColumns = (currentEnv: StudyEnvironment, currentEnvPath: string): ColumnDef<Notification>[] => [{
  id: 'notificationConfig',
  header: 'Notification',
  accessorKey: 'notificationConfig',
  cell: info => {
    return <NotificationConfigTypeDisplay config={info.row.original.notificationConfig}/>
  }
}, {
  id: 'method',
  header: 'Method',
  accessorKey: 'deliveryType'
}, {
  id: 'deliveryStatus',
  header: 'Status',
  accessorKey: 'deliveryStatus'
}, {
  id: 'opened',
  header: 'Opened',
  accessorKey: 'opened',
  cell: info => info.row.original.opened ?
    <FontAwesomeIcon icon={faEnvelopeOpen}/> : <FontAwesomeIcon icon={faEnvelope}/>
}, {
  id: 'createdAt',
  header: 'Date Sent',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.row.original.createdAt)
},
{
  id: 'config',
  header: 'Config',
  accessorKey: 'notificationConfig',
  cell: info => {
    const matchedConfig = currentEnv.notificationConfigs
      .find(cfg => cfg.id === info.row.original.notificationConfigId)
    return matchedConfig && <Link to={notificationConfigPath(matchedConfig, currentEnvPath)}>view</Link>
  }
}]

/** loads the list of notifications for a given enrollee and displays them in the UI */
export default function EnrolleeNotifications({ enrollee, studyEnvContext }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal, currentEnvPath } = studyEnvContext
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [isLoading, setIsLoading] = useState(true)

  const notificationTable = useReactTable({
    data: notifications,
    columns: notificationColumns(currentEnv, currentEnvPath),
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

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
      { basicTableLayout(notificationTable) }
      { renderEmptyMessage(notifications, 'No notifications') }

    </LoadingSpinner>
  </div>
}

