import React, { useState } from 'react'
import { Role } from '../api/adminUser'
import { ColumnDef, getCoreRowModel, getSortedRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import { useLoadingEffect } from '../api/api-utils'
import Api from '../api/api'

export const ROLE_COLUMNS: ColumnDef<Role>[] = [{
  header: 'Role',
  accessorKey: 'displayName'
}, {
  header: 'Description',
  accessorKey: 'description',
  enableSorting: false
}, {
  header: 'Permissions',
  id: 'moreDetail',
  enableSorting: false,
  cell: ({ row }) => <ul className="">
    {row.original.permissions.map(permission => <li key={permission.name}>
      {permission.displayName}: <span className="fst-italic text-muted">{permission.description}</span>
    </li>
    )}
  </ul>
}]

export default function RolesList() {
  const [roles, setRoles] = useState<Role[]>([])
  useLoadingEffect(async () => {
    const fetchedRoles = await Api.fetchRoles()
    setRoles(fetchedRoles)
  })
  return <div className="container p-3">
    Roles can be assigned to users to give them permissions to perform actions within a portal and study.
    A user may be assigned more than one role.
    <RoleTable roles={roles}/>
  </div>
}


export const RoleTable = ({ roles }: {roles: Role[]}) => {
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
