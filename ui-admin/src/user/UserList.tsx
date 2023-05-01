import React, { useEffect, useMemo, useState } from 'react'
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import Api, { AdminUser, Portal, PortalAdminUser } from 'api/api'
import { sortableTableHeader } from 'util/tableUtils'
import { instantToDefaultString } from 'util/timeUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faPlus } from '@fortawesome/free-solid-svg-icons'
import CreateUserModal from './CreateUserModal'

const UserList = () => {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [portals, setPortals] = React.useState<Portal[]>([])

  const portalColumn = (portalAdminUsers?: PortalAdminUser[]) => {
    if (!portalAdminUsers) {
      return ''
    }
    return portalAdminUsers.map(portalAdminUser =>
      portals.find(portal => portal.id === portalAdminUser.portalId)?.shortcode ?? '')
  }

  const columns: ColumnDef<AdminUser>[] = useMemo(() => ([{
    header: 'Username',
    accessorKey: 'username'
  }, {
    header: 'Superuser',
    accessorKey: 'superuser',
    cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }, {
    header: 'Portals',
    accessorKey: 'portalAdminUsers',
    cell: info => portalColumn(info.getValue() as PortalAdminUser[])
  }, {
    header: 'Created',
    accessorKey: 'createdAt',
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Last login',
    accessorKey: 'lastLogin',
    cell: info => instantToDefaultString(info.getValue() as number)
  }]), [users])

  const table = useReactTable({
    data: users,
    columns,
    state: {
      sorting
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  const loadUsers = async () => {
    setIsLoading(true)
    try {
      const result = await Promise.all(
        [Api.fetchUsers(), Api.getPortals()]
      )
      setUsers(result[0])
      setPortals(result[1])
    } catch (e) {
      alert(`error loading user list ${e}`)
    }
    setIsLoading(false)
  }

  const handleUserCreated = () => {
    // just reload everything
    loadUsers()
  }

  useEffect(() => {
    loadUsers()
  }, [])
  return <div className="container p-3">
    <h1 className="h4">All users </h1>
    <button className="btn-secondary btn" onClick={() => setShowCreateModal(true)}>
      <FontAwesomeIcon icon={faPlus}/> Create user
    </button>
    {showCreateModal && <CreateUserModal show={showCreateModal} setShow={setShowCreateModal} portals={portals}
      userCreated={handleUserCreated}/>}
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

export default UserList
