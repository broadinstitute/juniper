import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api from 'api/api'
import { Role } from 'api/adminUser'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { doApiLoad, useLoadingEffect } from '../api/api-utils'
import { RoleSelector } from './AdminUserDetail'

/** creates a new admin user */
const EditPortalUserModal = ({ userId, portalShortcode, onDismiss, userUpdated }:
  {
    userId: string,
    portalShortcode: string,
    onDismiss: () => void,
    userUpdated: () => void
  }) => {
  const [isLoading, setIsLoading] = useState(false)

  /** there might be a case for supporting updating usernames, but for now we only support updating roles */
  const [updateParams, setUpdateParams] = useState<{username: string, roleNames: string[]}>({
    username: '',
    roleNames: []
  })
  const [roles, setRoles] = useState<Role[]>([])

  useLoadingEffect(async () => {
    const [allRoles, loadedUser] = await Promise.all([
      Api.fetchRoles(),
      Api.fetchAdminUser(userId, portalShortcode)
    ])
    setUpdateParams({
      username: loadedUser.username,
      roleNames: loadedUser.portalAdminUsers![0].roles.map(role => role.name)
    })
    setRoles(allRoles)
  })

  const updateUser = async () => {
    doApiLoad(async () => {
      await Api.updatePortalUser(portalShortcode, userId, updateParams.roleNames)
      Store.addNotification(successNotification(`${updateParams.username} updated`))
      userUpdated()
      onDismiss()
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Update admin user</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <div className="mb-3">
            <label className="form-label">
              Email
              <input type="email" value={updateParams.username} className="form-control" size={50}
                disabled={true}/>
            </label>
          </div>
          <div>
            <RoleSelector roles={roles} selectedRoleNames={updateParams.roleNames} setSelectedRoleNames={roleNames =>
              setUpdateParams({ ...updateParams, roleNames })}/>
          </div>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={updateUser}>Save</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default EditPortalUserModal
