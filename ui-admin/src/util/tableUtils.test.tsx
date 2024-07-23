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
import { basicTableLayout, checkboxColumnCell, ColumnVisibilityControl, DownloadControl } from './tableUtils'
import { userEvent } from '@testing-library/user-event'

const SAMPLE_INITIAL_DATA = [{ consented: true, name: 'Fred' }, { consented: false, name: 'James' }]

/** simple table with filters, a download control, and a column control */
const TestTableComponent = ({ initialValue, initialData }: {
  initialValue: ColumnFiltersState, initialData: object[]
}) => {
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
    data: initialData,
    columns,
    state: { columnFilters },
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel()
  })

  return <div>
    <ColumnVisibilityControl table={table}/>
    <DownloadControl table={table} fileName={'test'}/>
    { basicTableLayout(table, { filterable: true }) }
  </div>
}

test('renders rows with default filters', async () => {
  render(<TestTableComponent initialValue={[{ id: 'consented', value: true }]} initialData={SAMPLE_INITIAL_DATA}/>)
  expect(screen.queryByText('James')).not.toBeInTheDocument()
  expect(screen.getByText('Fred')).toBeInTheDocument()
})

test('renders with no default filters', async () => {
  render(<TestTableComponent initialValue={[]} initialData={SAMPLE_INITIAL_DATA}/>)
  expect(screen.getByText('Fred')).toBeInTheDocument()
  expect(screen.getByText('James')).toBeInTheDocument()
})


test('show/hide column controls work', async () => {
  render(<TestTableComponent initialValue={[]} initialData={SAMPLE_INITIAL_DATA}/>)
  expect(screen.getByText('Columns')).toBeInTheDocument()
  expect(screen.getByText('Consented')).toBeInTheDocument()
  await userEvent.click(screen.getByText('Columns'))
  await waitFor(() => expect(screen.getByText('Toggle column visibility')).toBeVisible())
  await userEvent.click(screen.getByLabelText('Consented'))
  await userEvent.click(screen.getByText('Ok'))
  await waitFor(() => expect(screen.queryByText('Toggle column visibility')).not.toBeInTheDocument())
  expect(screen.queryByText('Consented')).not.toBeInTheDocument()
})

test('download button is enabled if there are rows in the table', async () => {
  render(<TestTableComponent initialValue={[]} initialData={SAMPLE_INITIAL_DATA}/>)
  const downloadButton = screen.getByText('Download')
  expect(downloadButton).toBeEnabled()
})

test('download button is disabled if there aren\'t any rows in the table', async () => {
  render(<TestTableComponent initialValue={[]} initialData={[]}/>)
  const downloadButton = screen.getByText('Download')
  expect(downloadButton).toHaveAttribute('aria-disabled', 'true')
})

test('download data modal should specify the number of rows to be downloaded', async () => {
  //Arrange
  render(<TestTableComponent initialValue={[]} initialData={SAMPLE_INITIAL_DATA}/>)
  const downloadButton = screen.getByText('Download')

  //Act
  await userEvent.click(downloadButton)

  //Assert
  const downloadText = screen.getByText('The current data filters', { exact: false })
  const rawTextContent = downloadText.textContent
  const expectedText = 'Download 2 rows to test.csv. ' +
    'The current data filters and shown columns will be applied.'
  expect(rawTextContent).toEqual(expectedText)
})
