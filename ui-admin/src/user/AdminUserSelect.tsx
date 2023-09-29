import React from 'react'
import { AdminUser } from '../api/api'
import Select from 'react-select'

export type AdminUserSelectProps = {
    selectedUser: AdminUser | undefined,
    setSelectedUser: (user: AdminUser | undefined) => void,
    users: AdminUser[]
    readOnly: boolean
    id: string
}

/** Renders a dropdown selector for admin users */
export default function AdminUserSelect(
  { selectedUser, setSelectedUser, users, readOnly=true, id }: AdminUserSelectProps) {
  const userOpts = [
    { label: 'Unassigned', value: undefined },
    ...users.map(user => ({ label: user.username, value: user }))
  ]
  const selectedOpt = userOpts.find(opt => opt.value === selectedUser)
  return <Select styles={{ control: baseStyles => ({ ...baseStyles, width: '400px' }) }}
    options={userOpts} isDisabled={readOnly} inputId={id}
    value={selectedOpt}

    onChange={opt => setSelectedUser(opt?.value)}/>
}
