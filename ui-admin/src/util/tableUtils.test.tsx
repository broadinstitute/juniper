import React, { useState } from 'react'
import { render, screen } from '@testing-library/react'

import {
  ColumnDef,
  ColumnFiltersState,
  getCoreRowModel,
  getFilteredRowModel,
  getSortedRowModel,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, checkboxColumnCell } from './tableUtils'

const TestFilterComponent = ({ initialValue }: {initialValue: ColumnFiltersState}) => {
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
    { basicTableLayout(table, { filterable: true }) }
  </div>
}

test('renders rows with default filters', async () => {
  render(<TestFilterComponent initialValue={[{ id: 'consented', value: true }]}/>)
  expect(screen.queryByText('James')).not.toBeInTheDocument()
  expect(screen.getByText('Fred')).toBeInTheDocument()
})

test('renders with no default filters', async () => {
  render(<TestFilterComponent initialValue={[]}/>)
  expect(screen.getByText('Fred')).toBeInTheDocument()
  expect(screen.getByText('James')).toBeInTheDocument()
})
