import React, { useEffect, useState } from 'react'
import Api, { MailingListContact, PortalEnvironment } from 'api/api'
import { LoadedPortalContextT } from './PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { sortableTableHeader } from '../util/tableUtils'
import { instantToDefaultString } from '../util/timeUtils'

const columns: ColumnDef<MailingListContact>[] = [{
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

export default function MailingListView({ portalContext, portalEnv }:
{portalContext: LoadedPortalContextT, portalEnv: PortalEnvironment}) {
  const [contacts, setContacts] = useState<MailingListContact[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])

  const table = useReactTable({
    data: contacts,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    debugTable: true
  })

  useEffect(() => {
    Api.fetchMailingList(portalContext.portal.shortcode, portalEnv.environmentName).then(result => {
      setContacts(result)
      setIsLoading(false)
    }).catch((e: Error) => {
      alert(`error loading mailing list ${  e}`)
    })
  }, [])
  return <div className="container p-3">
    <h1 className="h4">Mailing list </h1>
    <LoadingSpinner isLoading={isLoading}>
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
}
