import React, { useState } from 'react'
import { ParticipantTask } from 'api/api'

import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import {
  Enrollee,
  instantToDefaultString
} from '@juniper/ui-core'

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
    onRowSelectionChange: setRowSelection
  })

  return <div className="container p-3">
    <h1 className="h4">Tasks </h1>
    { basicTableLayout(table) }
  </div>
}

export default ParticipantTaskView

