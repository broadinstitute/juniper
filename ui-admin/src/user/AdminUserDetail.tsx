import React, { useState } from 'react'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { useLoadingEffect } from 'api/api-utils'
import { useParams } from 'react-router-dom'
import { ColumnDef, getCoreRowModel, getSortedRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { instantToDateString } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import { userHasPermission, useUser } from './UserProvider'
import { AdminUser, Role } from 'api/adminUser'

const ROLE_COLUMNS: ColumnDef<Role>[] = [{
  header: 'Role',
  accessorKey: 'displayName'
}, {
  header: 'Description',
  accessorKey: 'description'
}, {
  header: 'Permissions',
  id: 'moreDetail',
  cell: ({ row }) => <ul className="">
    {row.original.permissions.map(permission => <li key={permission.name}>
      {permission.displayName}: {permission.description}
    </li>
    )}
  </ul>
}]

/** shows roles and other details for an admin user */
export const AdminUserDetailRaw = ({ adminUserId, portalShortcode }:
  { adminUserId: string, portalShortcode?: string}) => {
  const { user: operator } = useUser()
  const [adminUser, setAdminUser] = useState<AdminUser>()
  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.fetchAdminUser(adminUserId, portalShortcode)
    setAdminUser(response)
  })
  const showMultiplePortalUsers = (adminUser?.portalAdminUsers && adminUser.portalAdminUsers.length > 1)


  return <div>
    {isLoading && <LoadingSpinner/>}
    {(!isLoading && adminUser) && <div className="container">
      <h2 className="h3">{adminUser?.username}</h2>
      { adminUser?.superuser && <dl>
        <dt>Superuser:</dt><dd><FontAwesomeIcon icon={faCheck}/></dd>
        <dt>Created:</dt><dd>{instantToDateString(adminUser.createdAt)}</dd>
      </dl> }
      { adminUser?.portalAdminUsers && adminUser?.portalAdminUsers.map(portalAdminUser => {
        return <div key={portalAdminUser.portalId}>
          {showMultiplePortalUsers && <h3 className="h5">portalId: {portalAdminUser.portalId}</h3>}
          <dl>
            <dt>Created:</dt><dd>{instantToDateString(adminUser.createdAt)}</dd>
            <dt>Last login:</dt><dd>
              {adminUser.lastLogin ? instantToDateString(adminUser.lastLogin) : 'none'}
            </dd>
          </dl>
          { userHasPermission(operator, portalAdminUser.portalId, 'team_roles_edit') &&
            <RoleTable roles={portalAdminUser.roles}/>
          }
        </div>
      }) }
    </div>}
  </div>
}

const RoleTable = ({ roles }: {roles: Role[]}) => {
  const roleTable = useReactTable({
    data: roles,
    columns: ROLE_COLUMNS,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })
  return <>
    { basicTableLayout(roleTable) }
  </>
}

/**
 * routable wrapper component for displaying details of an AdminUser
 */
const AdminUserDetail = ({ portalShortcode }: {portalShortcode?: string}) => {
  const { adminUserId } = useParams()
  if (!adminUserId) {
    return <div>
      No user id specified
    </div>
  }
  return <AdminUserDetailRaw adminUserId={adminUserId} portalShortcode={portalShortcode}/>
}

export default AdminUserDetail
