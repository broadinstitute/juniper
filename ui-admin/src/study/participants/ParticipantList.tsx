import React, { useState, useEffect, useMemo } from 'react'
import Api, { EnrolleeSearchResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { Link } from 'react-router-dom'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable, VisibilityState
} from '@tanstack/react-table'
import { ColumnVisibilityControl, IndeterminateCheckbox, sortableTableHeader } from '../../util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faTimes } from '@fortawesome/free-solid-svg-icons'


/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv, currentEnvPath } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchResult[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({
    'givenName': false,
    'familyName': false
  })


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
    cell: info => <Link to={`${currentEnvPath}/participants/${info.getValue()}`}>{info.getValue()}</Link>
  }, {
    id: 'familyName',
    header: 'Family name',
    accessorKey: 'profile.familyName'
  }, {
    id: 'givenName',
    header: 'Given name',
    accessorKey: 'profile.givenName'
  }, {
    header: 'Consented',
    accessorKey: 'enrollee.consented',
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }, {
    header: 'Withdrawn',
    accessorKey: 'enrollee.withdrawn',
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faTimes}/> : ''
  }], [study.shortcode])


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
    onRowSelectionChange: setRowSelection
  })

  useEffect(() => {
    Api.getEnrollees(portal.shortcode, study.shortcode, currentEnv.environmentName).then(result => {
      setParticipantList(result)
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification(`Error loading participants`))
    })
  }, [])
  return <div className="ParticipantList container pt-2">
    <div className="row">
      <div className="col-12">
        <h5>Participants</h5>
        <LoadingSpinner isLoading={isLoading}>
          <div className="d-flex align-items-center justify-content-between">
            <div>
              {Object.keys(rowSelection).length} of{' '}
              {table.getPreFilteredRowModel().rows.length} selected
            </div>
            <ColumnVisibilityControl table={table}/>
          </div>
          <table className="table table-striped">
            <thead>
              <tr>
                {table.getFlatHeaders().map(header => sortableTableHeader(header))}
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
        </LoadingSpinner>
      </div>
    </div>
  </div>
}


export default ParticipantList
