import React, { useState } from 'react'
import Api, { Enrollee, Event, Notification, Trigger } from 'api/api'
import { StudyEnvContextT, triggerPath } from '../../StudyEnvironmentRouter'
import LoadingSpinner from 'util/LoadingSpinner'
import { instantToDefaultString } from '@juniper/ui-core'
import TriggerTypeDisplay from '../../notifications/TriggerTypeDisplay'
import { Link } from 'react-router-dom'
import _capitalize from 'lodash/capitalize'
import _startCase from 'lodash/startCase'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { useLoadingEffect } from 'api/api-utils'


const isEvent = (val: Event | Notification): val is Event => {
  return Object.keys(val).includes('eventClass')
}

const isNotification = (val: Event | Notification): val is Notification => {
  return Object.keys(val).includes('notificationConfigId')
}

/** loads the list of notifications and events for a given enrollee and displays them in the UI */
export default function EnrolleeTimeline({ enrollee, studyEnvContext }:
                                           { enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal, currentEnvPath } = studyEnvContext
  const [tableData, setTableData] = useState<(Event | Notification)[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])

  const columns: ColumnDef<Event | Notification>[] = [
    {
      id: 'name',
      header: 'notification/event',
      cell: ({ row }) => {
        return <div>
          {isNotification(row.original) && <TriggerTypeDisplay config={row.original.trigger}/>}
          {isEvent(row.original) && _capitalize(_startCase(row.original.eventClass))}
        </div>
      }
    },
    {
      header: 'delivery type',
      accessorKey: 'deliveryType'
    },
    {
      header: 'delivery status',
      accessorKey: 'deliveryStatus'
    },
    {
      header: 'time',
      accessorKey: 'createdAt',
      cell: info => instantToDefaultString(info.getValue() as number)
    },
    {
      header: 'trigger',
      accessorKey: 'trigger',
      cell: info => info.getValue() && <Link
        to={triggerPath(info.getValue() as Trigger, currentEnvPath)}> trigger </Link>
    }
  ]

  /** matches each notification to a corresponding config by id */
  function attachConfigsToNotifications(rawNotifications: Notification[]) {
    rawNotifications.forEach(notification => {
      notification.trigger = currentEnv.triggers
        .find(config => config.id === notification.triggerId)
    })
  }

  const { isLoading } = useLoadingEffect(async () => {
    const [notifications, events] = await Promise.all([
      Api.fetchEnrolleeNotifications(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode),
      Api.fetchEnrolleeEvents(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode)
    ])
    attachConfigsToNotifications(notifications)
    setTableData([...notifications, ...events])
  }, [enrollee.shortcode])


  const table = useReactTable({
    data: tableData,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <div>
    <h5>Timeline</h5>
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

