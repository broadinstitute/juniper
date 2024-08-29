import React, { useId, useState } from 'react'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { useLoadingEffect } from 'api/api-utils'
import { Link, useParams } from 'react-router-dom'
import { instantToDateString } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faEdit } from '@fortawesome/free-solid-svg-icons'
import { AdminUser, Role } from 'api/adminUser'
import { RoleTable } from './RolesList'
import InfoPopup from '../components/forms/InfoPopup'
import Select from 'react-select'
import EditUserModal from './EditPortalUserModal'
import { Button } from '../components/forms/Button'


/** shows roles and other details for an admin user */
export const AdminUserDetailRaw = ({ adminUserId, portalShortcode }:
  { adminUserId: string, portalShortcode?: string}) => {
  const [adminUser, setAdminUser] = useState<AdminUser>()
  const [showEditModal, setShowEditModal] = useState(false)
  const { isLoading, reload } = useLoadingEffect(async () => {
    const user = await Api.fetchAdminUser(adminUserId, portalShortcode)
    setAdminUser(user)
  })
  const showMultiplePortalUsers = (adminUser?.portalAdminUsers && adminUser.portalAdminUsers.length > 1)
  const enableEditing = !adminUser?.superuser && portalShortcode

  return <div>
    {isLoading && <LoadingSpinner/>}
    {(!isLoading && adminUser) && <div className="container">
      <h2 className="h3">{adminUser?.username}</h2>
      <dl>
        <dt>Created:</dt><dd>{instantToDateString(adminUser.createdAt)}</dd>
        { adminUser?.superuser && <><dt>Superuser:</dt><dd><FontAwesomeIcon icon={faCheck}/></dd></> }
      </dl>
      { adminUser?.portalAdminUsers && adminUser?.portalAdminUsers.map(portalAdminUser => {
        return <div key={portalAdminUser.portalId}>
          {showMultiplePortalUsers && <h3 className="h5">portalId: {portalAdminUser.portalId}</h3>}
          <dl>
            <dt>Created:</dt><dd>{instantToDateString(adminUser.createdAt)}</dd>
            <dt>Last login:</dt><dd>
              {adminUser.lastLogin ? instantToDateString(adminUser.lastLogin) : 'none'}
            </dd>
          </dl>
          <RoleTable roles={portalAdminUser.roles}/>
          { enableEditing && <Button variant="secondary" outline={true} onClick={() => setShowEditModal(true)}>
            <FontAwesomeIcon icon={faEdit}/> Edit user roles
          </Button> }
        </div>
      }) }
      { showEditModal && <EditUserModal userId={adminUser!.id}
        portalShortcode={portalShortcode!}
        onDismiss={() => setShowEditModal(false)}
        userUpdated={reload}/>}
    </div>}
  </div>
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

export const RoleSelector = ({ roles, selectedRoleNames, setSelectedRoleNames }:
  { roles: Role[], selectedRoleNames: string[], setSelectedRoleNames: (roleNames: string[]) => void }) => {
  const inputId = useId()
  return <>
    <label className="form-label" htmlFor={inputId}>
    Roles <InfoPopup content={<span>See the full list of <Link to="roles" target="_blank">
                  roles and descriptions</Link>
      </span>}/>
    </label>
    <Select options={roles} inputId={inputId}
      value={selectedRoleNames.map(roleName => roles.find(role => role.name === roleName))}
      getOptionLabel={role => role!.displayName}
      getOptionValue={option => option!.name}
      isMulti={true}
      onChange={values => setSelectedRoleNames(values.map(value => value!.name))}/>
  </>
}

export default AdminUserDetail
