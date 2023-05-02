import React, { useEffect, useMemo, useState } from 'react'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import Api, { AdminUser, Portal } from 'api/api'
import { basicTableLayout } from 'util/tableUtils'
import { instantToDefaultString } from 'util/timeUtils'
import LoadingSpinner from 'util/LoadingSpinner'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import CreateUserModal from './CreateUserModal'
import { failureNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'

const PortalUserList = ({ portal }: {portal: Portal}) => {
  const [users, setUsers] = useState<AdminUser[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [sorting, setSorting] = React.useState<SortingState>([])

  const columns: ColumnDef<AdminUser>[] = useMemo(() => ([{
    header: 'Username',
    accessorKey: 'username'
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
      const result = await Api.fetchAdminUsersByPortal(portal.shortcode)
      setUsers(result)
    } catch (e) {
      Store.addNotification(failureNotification(`error loading user list`))
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
    {showCreateModal && <CreateUserModal show={showCreateModal} setShow={setShowCreateModal} portals={[portal]}
      userCreated={handleUserCreated}/>}
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
    </LoadingSpinner>
  </div>
}

export default PortalUserList
