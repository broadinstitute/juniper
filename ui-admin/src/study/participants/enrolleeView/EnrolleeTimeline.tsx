import React, { useEffect, useState } from 'react'
import Api, {
  Event,
  Notification,
  Trigger
} from 'api/api'
import {
  StudyEnvContextT,
  triggerPath
} from '../../StudyEnvironmentRouter'
import {
  Enrollee,
  instantToDefaultString
} from '@juniper/ui-core'
import TriggerTypeDisplay from '../../notifications/TriggerTypeDisplay'
import { Link } from 'react-router-dom'
import _capitalize from 'lodash/capitalize'
import _startCase from 'lodash/startCase'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, renderEmptyMessage } from 'util/tableUtils'
import { useLoadingEffect } from 'api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faEnvelope,
  faEnvelopeOpen
} from '@fortawesome/free-solid-svg-icons'
import InfoPopup from 'components/forms/InfoPopup'
import { InfoCard, InfoCardHeader } from '../../../components/InfoCard'
import LoadingSpinner from '../../../util/LoadingSpinner'
import Select from 'react-select'


const isEvent = (val: Event | Notification): val is Event => {
  return Object.keys(val).includes('eventClass')
}

const isNotification = (val: Event | Notification): val is Notification => {
  return Object.keys(val).includes('triggerId')
}

const EVENT_TYPES = [{
  value: 'notification',
  label: 'Notifications'
}, {
  value: 'event',
  label: 'Events'

}]

/** loads the list of notifications and events for a given enrollee and displays them in the UI */
export default function EnrolleeTimeline({ enrollee, studyEnvContext }:
                                           { enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal, currentEnvPath } = studyEnvContext
  const [tableData, setTableData] = useState<(Event | Notification)[]>([])
  const [filteredTableData, setFilteredTableData] = useState<(Event | Notification)[]>(tableData)
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [selectedEventTypes, setSelectedEventTypes] = useState<{ value: string, label: string }[]>(EVENT_TYPES)

  const columns: ColumnDef<Event | Notification>[] = [
    {
      id: 'name',
      header: 'Notification/Event',
      cell: ({ row }) => {
        return <div>
          {isNotification(row.original) && <TriggerTypeDisplay config={row.original.trigger}/>}
          {isEvent(row.original) && _capitalize(_startCase(row.original.eventClass))}
        </div>
      }
    },
    {
      header: 'Delivery Type',
      accessorKey: 'deliveryType'
    },
    {
      header: 'Delivery Status',
      accessorKey: 'deliveryStatus'
    },
    {
      id: 'opened',
      header: () =>
        <div className={'d-flex align-items-center'}>
          <span>Opened</span>
          <div onClick={e => e.stopPropagation()}><InfoPopup
            content={
            `Email activity may be unavailable due to a participant's email privacy
             settings. Additionally, emails sent more than 30 days ago may not have
             email activity associated with them.
             Note: please allow up to an hour for new activity to be reflected here.`
            }
          /></div>
        </div>,
      accessorKey: 'opened',
      cell: ({ row }) => {
        return isNotification(row.original) && renderEmailActivityIcon(row.original)
      }
    },
    {
      header: 'Time',
      accessorKey: 'createdAt',
      cell: info => instantToDefaultString(info.getValue() as number)
    },
    {
      header: 'Trigger',
      accessorKey: 'trigger',
      cell: info => info.getValue() && <Link
        to={triggerPath(info.getValue() as Trigger, currentEnvPath)}> config </Link>
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

  useEffect(() => {
    setFilteredTableData(tableData.filter(row => {
      if (selectedEventTypes.length === 0) {
        return false
      }
      return selectedEventTypes.some(type => {
        if (type.value === 'notification') {
          return isNotification(row)
        }
        if (type.value === 'event') {
          return isEvent(row)
        }
        return false
      })
    }))
  }, [tableData, selectedEventTypes])


  const table = useReactTable({
    data: filteredTableData,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <InfoCard>
    <InfoCardHeader>
      <div className="d-flex justify-content-between align-items-center w-100">
        <div className="fw-bold lead my-1">Timeline</div>
        <Select className="m-1"
          placeholder={'Filter by event type...'}
          options={EVENT_TYPES} isMulti={true} value={selectedEventTypes}
          onChange={selected => setSelectedEventTypes(selected as { value: string, label: string }[])}
        />
      </div>
    </InfoCardHeader>
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table, { tableClass: 'table m-0' })}
    </LoadingSpinner>
    {<div className='my-3'>
      {renderEmptyMessage(tableData, 'No timeline events')}
      {renderEmptyMessage(filteredTableData, 'No timeline events. Are your filters correct?')}
    </div>}
  </InfoCard>
}

/**
 * Renders an icon to indicate whether an email has been opened
 */
export function renderEmailActivityIcon(row: Notification) {
  const notificationEventDetails = row.eventDetails
  if (notificationEventDetails && notificationEventDetails.opensCount > 0) {
    return <FontAwesomeIcon icon={faEnvelopeOpen} aria-label={'Email opened'}/>
  }

  if (notificationEventDetails) {
    return <FontAwesomeIcon icon={faEnvelope} aria-label={'Email not yet opened'}/>
  }

  return <span className="fw-light fst-italic">n/a</span>
}
