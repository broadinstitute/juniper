import React, { useState } from 'react'
import { render, screen, waitFor } from '@testing-library/react'

import {
  ColumnDef,
  ColumnFiltersState,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, checkboxColumnCell, ColumnVisibilityControl } from './tableUtils'
import userEvent from '@testing-library/user-event'

/** simple table with filters and a show/hide column control */
const TestTableComponent = ({ initialValue }: {initialValue: ColumnFiltersState}) => {
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>(
    initialValue
  )

  const columns: ColumnDef<object, boolean>[] = [{
    header: 'Consented',
    accessorKey: 'consented',
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Consented' },
        { value: false, label: 'Not Consented' }
      ]
    },
    filterFn: 'equals',
    cell: checkboxColumnCell
  }, {
    header: 'Name',
    accessorKey: 'name'
  }]

  const table = useReactTable({
    data: [{ consented: true, name: 'Fred' }, { consented: false, name: 'James' }],
    columns,
    state: { columnFilters },
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel()
  })

  return <div>
    <ColumnVisibilityControl table={table}/>
    { basicTableLayout(table, { filterable: true }) }
  </div>
}

test('renders rows with default filters', async () => {
  render(<TestTableComponent initialValue={[{ id: 'consented', value: true }]}/>)
  expect(screen.queryByText('James')).not.toBeInTheDocument()
  expect(screen.getByText('Fred')).toBeInTheDocument()
})

test('renders with no default filters', async () => {
  render(<TestTableComponent initialValue={[]}/>)
  expect(screen.getByText('Fred')).toBeInTheDocument()
  expect(screen.getByText('James')).toBeInTheDocument()
})


test('show/hide column controls work', async () => {
  render(<TestTableComponent initialValue={[]}/>)
  expect(screen.getByText('Show/hide columns')).toBeInTheDocument()
  expect(screen.getByText('Consented')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Show/hide columns'))
  await waitFor(() => expect(screen.getByText('Toggle column visibility')).toBeVisible())
  await userEvent.click(screen.getByLabelText('Consented'))
  await userEvent.click(screen.getByText('Ok'))
  await waitFor(() => expect(screen.queryByText('Toggle column visibility')).not.toBeInTheDocument())
  expect(screen.queryByText('Consented')).not.toBeInTheDocument()
})
