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
import UserAction from './UserAction'
import { Button } from 'components/forms/Button'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
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
  }, {
    header: 'Actions',
    accessorKey: 'actions',
    cell: info => <UserAction row={info.row} portal={portal} onUserListChanged={handleUserListChanged}/>
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

  const handleUserListChanged = () => {
    // just reload everything
    loadUsers()
  }

  useEffect(() => {
    loadUsers()
  }, [])
  return <div className="container-fluid px-4 py-2">
    <div className="align-items-baseline d-flex mb-2">
      <h2 className="text-center me-4 fw-bold">Manage Team</h2>
    </div>
    <div className="d-flex align-items-center justify-content-between">
      <div className="d-flex">
        <h4>All users</h4>
      </div>
      <div className="d-flex">
        <Button variant="light" className="border m-1" onClick={() => setShowCreateModal(true)}>
          <FontAwesomeIcon icon={faPlus}/> Create user
        </Button>
      </div>
    </div>
    {showCreateModal && <CreateUserModal onDismiss={() => setShowCreateModal(false)} portals={[portal]}
      userCreated={handleUserListChanged}/>}
    <LoadingSpinner isLoading={isLoading}>
      {basicTableLayout(table)}
      { users.length === 0 &&
        <span className="d-flex justify-content-center text-muted fst-italic">No users</span> }
    </LoadingSpinner>
  </div>
}

export default PortalUserList
