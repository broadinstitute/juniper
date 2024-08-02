import React, { useEffect, useState } from 'react'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState
} from '@tanstack/react-table'
import { instantToDefaultString, LogEvent } from '@juniper/ui-core'
import { basicTableLayout, ColumnVisibilityControl } from 'util/tableUtils'
import { useUser } from 'user/UserProvider'
import { renderPageHeader } from 'util/pageUtils'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faInfoCircle, faUpRightAndDownLeftFromCenter } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import pluralize from 'pluralize'

/** displays a filterable and sortable table of recent Log Events */
export default function LogEventViewer() {
  const eventTypes = [
    { value: 'ERROR', label: 'Error' },
    { value: 'ACCESS', label: 'Access' },
    { value: 'EVENT', label: 'Event' },
    { value: 'STATS', label: 'Stats' },
    { value: 'INFO', label: 'Info' }
  ]
  const dateRanges = [
    { value: '1', label: 'Last 24 Hours' },
    { value: '7', label: 'Last Week' },
    { value: '30', label: 'Last Month' }
  ]
  const [selectedEventTypes, setSelectedEventTypes] = useState<{ value: string, label: string }[]>(
    eventTypes.filter(e => e.value != 'STATS'))
  const [selectedDateRange, setSelectedDateRange] = useState<{ value: string, label: string }>(
    { value: '7', label: 'Last Week' })
  const [logEvents, setLogEvents] = useState<LogEvent[]>([])
  const { user } = useUser()

  const { isLoading } = useLoadingEffect(async () => {
    await loadLogEvents()
  })

  const loadLogEvents = async () => {
    const response = await Api.loadLogEvents(
      selectedEventTypes.map(e => e.value), selectedDateRange.value)
    setLogEvents(response)
  }

  useEffect(() => {
    loadLogEvents()
  }, [selectedEventTypes, selectedDateRange])

  const [sorting, setSorting] = React.useState<SortingState>([
    { id: 'createdAt', desc: true }
  ])

  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'id': false,
    'studyShortcode': false,
    'operatorId': false,
    'enrolleeShortcode': false
  })

  const columns: ColumnDef<LogEvent>[] = [{
    header: 'Event ID',
    accessorKey: 'id'
  }, {
    header: 'Type',
    accessorKey: 'eventType'
  }, {
    header: 'Name',
    accessorKey: 'eventName'
  }, {
    header: 'Source',
    accessorKey: 'eventSource'
  }, {
    header: 'Stacktrace',
    accessorKey: 'stackTrace',
    cell: info => viewFullJsonButton(info.getValue() as string)
  }, {
    header: 'Detail',
    accessorKey: 'eventDetail',
    cell: info => viewFullJsonButton(info.getValue() as string)
  }, {
    header: 'Portal Shortcode',
    accessorKey: 'portalShortcode'
  }, {
    header: 'Study Shortcode',
    accessorKey: 'studyShortcode'
  }, {
    header: 'Environment',
    accessorKey: 'environmentName'
  }, {
    header: 'Operator ID',
    accessorKey: 'operatorId'
  }, {
    header: 'Created At',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }]

  const table = useReactTable({
    data: logEvents,
    columns,
    state: {
      sorting,
      columnVisibility
    },
    onColumnVisibilityChange: setColumnVisibility,
    onSortingChange: setSorting,
    enableRowSelection: true,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return (
    <div className="px-4 py-2">
      {renderPageHeader('Log Events')}
      <span className="text-muted fst-italic">
        <FontAwesomeIcon icon={faInfoCircle} className="me-2"/>
        This table is limited to 1000 log events. If you need to view more, contact support.
      </span>
      <div className="mt-4">
        {isLoading && <LoadingSpinner/>}
      </div>
      <div className="d-flex align-items-center justify-content-between">
        <span>
          {pluralize('log event', logEvents.length, true)}
        </span>
        <div className="d-flex align-items-center">
          <Select className="m-1" options={eventTypes} isMulti={true} value={selectedEventTypes}
            onChange={selected => setSelectedEventTypes(selected as { value: string, label: string }[])}
          />
          <Select className="m-1" options={dateRanges} value={selectedDateRange}
            onChange={selected => setSelectedDateRange(selected as { value: string, label: string })}
          />
          <ColumnVisibilityControl table={table}/>
        </div>
      </div>
      <div className="mt-4">
        {user?.superuser ?
          <>{logEvents.length > 0 ?
            basicTableLayout(table) :
            <span className="d-flex justify-content-center text-muted fst-italic">
              No log events returned. Are your filters correct?
            </span>
          }</>
          :
          <div>You do not have permission to view this page</div>
        }
      </div>
    </div>
  )
}

const viewFullJsonButton = (json: string) => {
  const [open, setOpen] = useState(false)

  if (!json) { return <span className="fst-italic text-muted">No value</span> }

  return <>
    <Button
      className="px-0"
      variant="link"
      onClick={() => {
        setOpen(!open)
      }}
    >View full<FontAwesomeIcon className="mx-2" icon={faUpRightAndDownLeftFromCenter}/></Button>
    {open && <EventDetailModal json={json} onDismiss={() => setOpen(false)}/>}
  </>
}

const EventDetailModal = ({ json, onDismiss }: { json: string,
    onDismiss: () => void
}) => {
  const stringValue = (() => {
    try {
      return JSON.stringify(JSON.parse(json), null, 2)
    } catch (e) {
      return json
    }
  })()

  return <Modal show={true}
    onHide={onDismiss}
  >
    <Modal.Header>
      <Modal.Title>View full details</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <pre>
        {stringValue}
      </pre>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={onDismiss}
      >Close
      </button>
    </Modal.Footer>
  </Modal>
}
