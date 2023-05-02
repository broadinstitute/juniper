import React, { useEffect, useState } from 'react'
import Api, { MailingListContact, PortalEnvironment } from 'api/api'
import { LoadedPortalContextT } from './PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, IndeterminateCheckbox } from '../util/tableUtils'
import { instantToDefaultString } from '../util/timeUtils'

const columns: ColumnDef<MailingListContact>[] = [{
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
  header: 'Email',
  accessorKey: 'email'
}, {
  header: 'Name',
  accessorKey: 'name'
}, {
  header: 'Joined',
  accessorKey: 'createdAt',
  cell: info => instantToDefaultString(info.getValue() as number)
}]

/** show the mailing list in table */
export default function MailingListView({ portalContext, portalEnv }:
{portalContext: LoadedPortalContextT, portalEnv: PortalEnvironment}) {
  const [contacts, setContacts] = useState<MailingListContact[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})

  const table = useReactTable({
    data: contacts,
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

  useEffect(() => {
    Api.fetchMailingList(portalContext.portal.shortcode, portalEnv.environmentName).then(result => {
      setContacts(result)
      setIsLoading(false)
    }).catch((e: Error) => {
      alert(`error loading mailing list ${  e}`)
      setIsLoading(false)
    })
  }, [])
  return <div className="container p-3">
    <h1 className="h4">Mailing list </h1>
    <LoadingSpinner isLoading={isLoading}>
      <div>
        {Object.keys(rowSelection).length} of{' '}
        {table.getPreFilteredRowModel().rows.length} selected
      </div>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}
