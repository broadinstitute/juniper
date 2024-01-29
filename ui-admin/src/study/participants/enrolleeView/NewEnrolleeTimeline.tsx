import Api, { DataChangeRecord, Enrollee, Event, Notification, Trigger } from '../../../api/api'
import { StudyEnvContextT, triggerPath } from '../../StudyEnvironmentRouter'
import React, { useState } from 'react'
import LoadingSpinner from '../../../util/LoadingSpinner'
import { basicTableLayout } from '../../../util/tableUtils'
import { useLoadingEffect } from '../../../api/api-utils'
import {
  ColumnDef,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { instantToDefaultString } from '../../../util/timeUtils'
import { faArrowRight, faUpRightAndDownLeftFromCenter } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import _capitalize from 'lodash/capitalize'
import _startCase from 'lodash/startCase'
import Modal from 'react-bootstrap/Modal'
import { findDifferencesBetweenObjects, ObjectDiff } from '../../../util/objectUtils'
import { isEmpty } from 'lodash'
import { Link } from 'react-router-dom'


type EnrolleeEvent = Event | Notification | DataChangeRecord

const isEvent = (val: EnrolleeEvent): val is Event => {
  return Object.keys(val).includes('eventClass')
}

const isNotification = (val: EnrolleeEvent): val is Notification => {
  return Object.keys(val).includes('triggerId')
}

const isDataChangeRecord = (val: EnrolleeEvent): val is DataChangeRecord => {
  return Object.keys(val).includes('modelName')
}

/**
 *
 */
export default function EnrolleeTimeline({ enrollee, studyEnvContext }:
{ enrollee: Enrollee, studyEnvContext: StudyEnvContextT }) {
  const { currentEnv, study, portal, currentEnvPath } = studyEnvContext
  const [tableData, setTableData] = useState<(EnrolleeEvent)[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [adminNames] = React.useState<Map<string, string>>(new Map)

  const [globalFilter, setGlobalFilter] = useState('')

  const [showExpandedDataChangeRecord, setShowExpandedDataChangeRecord] = useState<DataChangeRecord | undefined>()
  const [showExpandedNotification, setShowExpandedNotification] = useState<Notification | undefined>()

  const getSummaryString = (row: EnrolleeEvent) => {
    if (isEvent(row)) {
      return _capitalize(_startCase(row.eventClass))
    } else if (isNotification(row)) {
      return  `${_capitalize(row.deliveryStatus)  } ${  _startCase(row.trigger?.triggerType).toLowerCase()}`
    } else if (isDataChangeRecord(row)) {
      return `${row.modelName} updated`
    }
    return ''
  }

  const getResponsibleUserStr = (dataChangeRecord: DataChangeRecord): string => {
    // look through caches for participant and admin data
    if (dataChangeRecord.responsibleAdminUserId) {
      return `Admin (${adminNames.get(dataChangeRecord.responsibleAdminUserId)})`
    } else if (dataChangeRecord.responsibleUserId) {
      return 'Participant'
    }
    return 'System'
  }

  /** matches each notification to a corresponding config by id */
  function attachConfigsToNotifications(rawNotifications: Notification[]) {
    rawNotifications.forEach(notification => {
      notification.trigger = currentEnv.triggers
        .find(config => config.id === notification.triggerId)
    })
  }

  const { isLoading } = useLoadingEffect(async () => {
    const [notifications, events, dataChangeRecords, adminUsers] = await Promise.all([
      Api.fetchEnrolleeNotifications(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode),
      Api.fetchEnrolleeEvents(portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode),
      Api.fetchEnrolleeChangeRecords(
        portal.shortcode,
        study.shortcode,
        currentEnv.environmentName,
        enrollee.shortcode
      ),
      Api.fetchAdminUsers()
    ])

    adminUsers.forEach(adminUser => {
      adminNames.set(adminUser.id, adminUser.username.split('@')[0])
    })

    attachConfigsToNotifications(notifications)
    setTableData([...notifications, ...events, ...dataChangeRecords])
  }, [enrollee.shortcode])

  const columns: ColumnDef<EnrolleeEvent>[] = [
    {
      header: 'time',
      accessorKey: 'createdAt',
      accessorFn: originalRow => instantToDefaultString(originalRow.createdAt as number),
      enableGlobalFilter: true
    },
    {
      id: 'type',
      header: 'type',
      accessorFn: row => {
        if (isNotification(row)) {
          return 'Notification'
        } else if (isEvent(row)) {
          return 'Event'
        } else if (isDataChangeRecord(row)) {
          return 'Data Change'
        }
      },
      enableGlobalFilter: true
    },
    {
      header: 'summary',
      accessorFn: getSummaryString,
      enableGlobalFilter: true
    },
    {
      header: 'responsible user',
      accessorFn: row => {
        if (isDataChangeRecord(row)) {
          return getResponsibleUserStr(row)
        }
        return ''
      },
      enableGlobalFilter: true
    },
    {
      id: 'detail',
      header: '',
      cell: ({ row }) => {
        if (isEvent(row.original)) {
          return <></>
        }
        return <button className="btn btn-link" onClick={() => {
          if (isNotification(row.original)) {
            setShowExpandedNotification(row.original)
          } else if (isDataChangeRecord(row.original)) {
            setShowExpandedDataChangeRecord(row.original)
          }
        }}>
          <FontAwesomeIcon icon={faUpRightAndDownLeftFromCenter}/>
        </button>
      }
    }
  ]

  const table = useReactTable({
    data: tableData,
    columns,
    state: {
      sorting,
      globalFilter
    },
    onSortingChange: setSorting,
    onGlobalFilterChange: setGlobalFilter,
    enableGlobalFilter: true,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <div>
    <h5>Timeline</h5>
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex">
        <label className="form-label m-2">Search</label>
        <input className="form-control mb-2 w-25"
          value={globalFilter} onChange={e => setGlobalFilter(e.target.value)}/>
      </div>
      {basicTableLayout(table)}

      {showExpandedDataChangeRecord && (
        <ExpandedDataChangeRecord
          dataChangeRecord={showExpandedDataChangeRecord}
          onDismiss={() => setShowExpandedDataChangeRecord(undefined)}/>
      )}

      {showExpandedNotification && (
        <ExpandedNotification
          notification={showExpandedNotification}
          onDismiss={() => setShowExpandedNotification(undefined)}
          currentEnvPath={currentEnvPath} />
      )}

    </LoadingSpinner>
  </div>
}

const ExpandedDataChangeRecord = (
  { dataChangeRecord, onDismiss } : { dataChangeRecord: DataChangeRecord, onDismiss: () => void }
) => {
  const calcChanges = (): ObjectDiff[] => {
    try {
      const newObject: { [index: string]: object } =
        isEmpty(dataChangeRecord.newValue) ? {} : JSON.parse(dataChangeRecord.newValue)
      const oldObject: { [index: string]: object } =
        isEmpty(dataChangeRecord.oldValue) ? {} : JSON.parse(dataChangeRecord.oldValue)

      if ((newObject && typeof newObject === 'object') && (oldObject && typeof oldObject === 'object')) {
        return findDifferencesBetweenObjects(oldObject, newObject)
      }

      return [
        {
          oldValue: dataChangeRecord.oldValue,
          newValue: dataChangeRecord.newValue,
          fieldName: dataChangeRecord.fieldName || ''
        }
      ]
    } catch (e) {
      return [
        {
          oldValue: dataChangeRecord.oldValue,
          newValue: dataChangeRecord.newValue,
          fieldName: dataChangeRecord.fieldName || ''
        }
      ]
    }
  }

  const changes: ObjectDiff[] = calcChanges()

  return <Modal show={true} onHide={onDismiss} size={'lg'}>
    <Modal.Header>
      <Modal.Title>
          Data Change Record
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="border-start border-3 p-1 ps-2 border-warning w-75 ms-4 mb-4"
        style={{ backgroundColor: '#f2f2f2' }}>
        <p className={'fw-bold mb-0'}>Changes</p>
        {changes.map((change, idx) =>
          <p key={idx} className="mb-0">
            {change.fieldName}: {change.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {change.newValue}
          </p>
        )}
      </div>


      <h5>Justification</h5>
      {isEmpty(dataChangeRecord.justification)
        ? <p className="fst-italic"> None provided</p>
        : <p>{dataChangeRecord.justification}</p>}

    </Modal.Body>
  </Modal>
}

const ExpandedNotification = (
  { notification, onDismiss, currentEnvPath }:
    { notification: Notification, onDismiss: () => void, currentEnvPath: string }
) => {
  return <Modal show={true} onHide={onDismiss} size={'lg'}>
    <Modal.Header>
      <Modal.Title>
        Notification
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>Trigger Type: {notification.trigger?.triggerType}</p>
      <p>Delivered Via: {notification.deliveryType}</p>
      <p>Status: {notification.deliveryStatus}</p>
      <p>Sent To: {notification.sentTo}</p>
      <p>Retries: {notification.retries}</p>
      {notification.trigger && <Link to={triggerPath(notification.trigger as Trigger, currentEnvPath)}>
          Go To Trigger Config
      </Link>}
    </Modal.Body>
  </Modal>
}
