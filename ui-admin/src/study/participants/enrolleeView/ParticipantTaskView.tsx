import React, { useState } from 'react'
import { ParticipantTask, Enrollee } from 'api/api'

import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { tableHeader } from 'util/tableUtils'
import { instantToDefaultString } from '@juniper/ui-core'

const columns: ColumnDef<ParticipantTask>[] = [{
  header: 'Task',
  accessorKey: 'taskType'
}, {
  header: 'Item',
  id: 'targetName',
  cell: info => <span>
    {info.row.original.targetName}
    <span className="fw-light fst-italic"> v{info.row.original.targetAssignedVersion}</span>
  </span>
}, {
  header: 'status',
  accessorKey: 'status'
}, {
  header: 'asssigned',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.getValue() as number)
}, {
  header: 'completed',
  accessorKey: 'completedAt',
  cell: info => instantToDefaultString(info.getValue() as number)
}]

/** show the task list in table */
const ParticipantTaskView = ({ enrollee }: {enrollee: Enrollee}) => {
  const [sorting, setSorting] = useState<SortingState>([{ id: 'createdAt', desc: true }])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})

  const table = useReactTable({
    data: enrollee.participantTasks,
    columns,
    state: {
      sorting,
      rowSelection
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onRowSelectionChange: setRowSelection,
    debugTable: true
  })

  return <div className="container p-3">
    <h1 className="h4">Tasks </h1>
    <table className="table table-striped">
      <thead>
        <tr>
          {table.getFlatHeaders().map(header => tableHeader(header, { sortable: true, filterable: false }))}
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
  </div>
}

export default ParticipantTaskView

