import React, { useState, useEffect, useMemo } from 'react'
import Api, { EnrolleeSearchResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { Link, useSearchParams } from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable, VisibilityState
} from '@tanstack/react-table'
import { ColumnVisibilityControl, IndeterminateCheckbox, tableHeader } from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import AdHocEmailModal from '../AdHocEmailModal'
import { facetValuesFromString, SAMPLE_FACETS, FacetValue }
  from 'api/enrolleeSearch'
import { Button } from 'components/forms/Button'
import { instantToDefaultString } from 'util/timeUtils'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchResult[]>([])
  const [showEmailModal, setShowEmailModal] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([
    {id: 'createdAt', desc: true}
  ])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'givenName': false,
    'familyName': false,
    'contactEmail': false
  })
  const [searchParams] = useSearchParams()

  const facetValues = facetValuesFromString(searchParams.get('facets') ?? '{}', SAMPLE_FACETS)


  const columns = useMemo<ColumnDef<EnrolleeSearchResult, string>[]>(() => [{
    id: 'select',
    header: ({ table }) => <IndeterminateCheckbox
      checked={table.getIsAllRowsSelected()} indeterminate={table.getIsSomeRowsSelected()}
      onChange={table.getToggleAllRowsSelectedHandler()}/>,
    cell: ({ row }) => (
      <div className="px-1">
        <IndeterminateCheckbox
          checked={row.getIsSelected()} indeterminate={row.getIsSomeSelected()}
          onChange={row.getToggleSelectedHandler()} disabled={!row.getCanSelect()}/>
      </div>
    )
  }, {
    header: 'Shortcode',
    accessorKey: 'enrollee.shortcode',
    meta: {
      columnType: 'string'
    },
    cell: info => <Link to={`${currentEnvPath}/participants/${info.getValue()}`}>{info.getValue()}</Link>
  }, {
    header: 'Created',
    id: 'createdAt',
    accessorKey: 'enrollee.createdAt',
    enableColumnFilter: false,
    cell: info => instantToDefaultString(info.getValue() as unknown as number)
  }, {
    id: 'familyName',
    header: 'Family name',
    accessorKey: 'profile.familyName',
    meta: {
      columnType: 'string'
    }
  }, {
    id: 'givenName',
    header: 'Given name',
    accessorKey: 'profile.givenName',
    meta: {
      columnType: 'string'
    }
  }, {
    id: 'contactEmail',
    header: 'Contact email',
    accessorKey: 'profile.contactEmail',
    meta: {
      columnType: 'string'
    }
  }, {
    header: 'Consented',
    accessorKey: 'enrollee.consented',
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Consented' },
        { value: false, label: 'Not Consented' }
      ]
    },
    filterFn: 'equals',
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }, {
    header: 'Kit status',
    filterFn: 'includesString', //an undefined value in a cell seems to switch the filter table away from the default
    accessorKey: 'mostRecentKitStatus',
    meta: {
      columnType: 'string'
    }
  }], [study.shortcode, currentEnv.environmentName])


  const table = useReactTable({
    data: participantList,
    columns,
    state: {
      sorting,
      rowSelection,
      columnVisibility
    },
    onColumnVisibilityChange: setColumnVisibility,
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onRowSelectionChange: setRowSelection
  })

  const searchEnrollees = async (portalShortcode: string, studyShortcode: string,
    envName: string, facetValues: FacetValue[]) => {
    setIsLoading(true)
    try {
      const response = await Api.searchEnrollees(portalShortcode, studyShortcode, envName,
        facetValues)
      setParticipantList(response)
    } catch (e) {
      Store.addNotification(failureNotification('Error loading participants'))
    }
    setIsLoading(false)
  }

  useEffect(() => {
    searchEnrollees(portal.shortcode, study.shortcode, currentEnv.environmentName, facetValues)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName])

  const numSelected = Object.keys(rowSelection).length
  const allowSendEmail = numSelected > 0
  const enrolleesSelected = Object.keys(rowSelection)
    .filter(key => rowSelection[key])
    .map(key => participantList[parseInt(key)].enrollee.shortcode)

  return <div className="ParticipantList container pt-2">
    <div className="row">
      <div className="col-12 align-items-baseline d-flex">
        <h2 className="h4 text-center me-4">{study.name} Participants</h2>
      </div>
      <div className="col-12">
        <LoadingSpinner isLoading={isLoading}>
          <div>
            <div className="d-flex align-items-center">
              <span className="me-2">
                {numSelected} of{' '}
                {table.getPreFilteredRowModel().rows.length} selected ({table.getFilteredRowModel().rows.length} shown)
              </span>
              <span className="me-2">
                <Button onClick={() => setShowEmailModal(allowSendEmail)}
                  variant="link" disabled={!allowSendEmail}
                  tooltip={allowSendEmail ? 'Send email' : 'Select at least one participant'}>
                  Send email
                </Button>
              </span>
              { showEmailModal && <AdHocEmailModal enrolleeShortcodes={enrolleesSelected}
                studyEnvContext={studyEnvContext}
                onDismiss={() => setShowEmailModal(false)}/> }
              <ColumnVisibilityControl table={table}/>
            </div>
          </div>
          <table className="table table-striped">
            <thead>
              <tr>
                {table.getFlatHeaders().map(header => tableHeader(header, { sortable: true, filterable: true }))}
              </tr>
            </thead>
            <tbody>
              {table.getRowModel().rows.map(row => {
                return (
                  <tr key={row.id}>
                    {row.getVisibleCells().map(cell => {
                      return (
                        <td key={cell.id}>
                          {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </td>
                      )
                    })}
                  </tr>
                )
              })}
            </tbody>
          </table>
          { participantList.length === 0 && <span className="text-muted fst-italic">No participants</span>}
        </LoadingSpinner>
      </div>
    </div>
  </div>
}


export default ParticipantList
