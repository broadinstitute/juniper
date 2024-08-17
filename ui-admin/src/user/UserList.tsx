import React, { useMemo, useState } from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import Api, { Portal } from 'api/api'
import { AdminUser, PortalAdminUser } from 'api/adminUser'
import { useLoadingEffect } from 'api/api-utils'
import { basicTableLayout, renderEmptyMessage }  from 'util/tableUtils'
import { instantToDefaultString } from '@juniper/ui-core'
import LoadingSpinner from 'util/LoadingSpinner'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faPlus } from '@fortawesome/free-solid-svg-icons'
import CreateUserModal from './CreateUserModal'
import { Button } from 'components/forms/Button'
import { renderPageHeader } from 'util/pageUtils'
import { Link } from 'react-router-dom'
import UserAction from './UserAction'

/** lists all admin users or the users of a specific portal */
const UserList = ({ portal }: {portal?: Portal}) => {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [portals, setPortals] = React.useState<Portal[]>([])

  const portalColumn = (portalAdminUsers?: PortalAdminUser[]) => {
    if (!portalAdminUsers) {
      return ''
    }
    return portalAdminUsers.map(portalAdminUser =>
      portals.find(portal => portal.id === portalAdminUser.portalId)?.shortcode ?? '')
      .join(', ')
  }

  const columns: ColumnDef<AdminUser>[] = useMemo(() => {
    const cols: ColumnDef<AdminUser>[] = [{
      header: 'Username',
      id: 'username',
      accessorKey: 'username',
      cell: ({ row }) => <Link to={row.original.id}>{row.original.username}</Link>
    }]
    if (!portal) {
      cols.push({
        header: 'Superuser',
        accessorKey: 'superuser',
        cell: info => info.getValue() ? <FontAwesomeIcon icon={faCheck} aria-label="yes"/> : ''
      })
      cols.push({
        header: 'Portals',
        accessorKey: 'portalAdminUsers',
        cell: info => portalColumn(info.getValue() as PortalAdminUser[])
      })
    }
    cols.push({
      header: 'Created',
      accessorKey: 'createdAt',
      cell: info => instantToDefaultString(info.getValue() as number)
    })
    cols.push({
      header: 'Last login',
      accessorKey: 'lastLogin',
      cell: info => instantToDefaultString(info.getValue() as number)
    })
    cols.push({
      header: 'Actions',
      accessorKey: 'actions',
      cell: info => <UserAction row={info.row} portal={portal} onUserListChanged={reload}/>
    })
    return cols
  }, [users])

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

  const { isLoading, reload } = useLoadingEffect(async () => {
    try {
      let result
      if (portal) {
        result = await Promise.all([Api.fetchAdminUsersByPortal(portal.shortcode), Api.getPortals()])
      } else {
        result = await Promise.all([Api.fetchAdminUsers(), Api.getPortals()])
      }
      setUsers(result[0])
      setPortals(result[1])
    } catch (e) {
      alert(`error loading user list ${e}`)
    }
  })

  const handleUserCreated = () => {
    // just reload everything
    reload()
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('All Users') }
    <div className="d-flex align-items-center justify-content-end">
      <Button variant="light" className="border m-1" onClick={() => setShowCreateModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Create user
      </Button>
    </div>
    {showCreateModal && <CreateUserModal onDismiss={() => setShowCreateModal(false)}
      portals={portal ? [portal] : portals}
      userCreated={handleUserCreated}/>}
    <LoadingSpinner isLoading={isLoading}>
      { basicTableLayout(table) }
      { renderEmptyMessage(users, 'No users') }
    </LoadingSpinner>
  </div>
}

export default UserList
