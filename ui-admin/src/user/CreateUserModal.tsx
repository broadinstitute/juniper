import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { Portal } from 'api/api'
import { AdminUser, NewAdminUser, Role } from 'api/adminUser'
import { useUser } from './UserProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import Select from 'react-select'
import { doApiLoad, useLoadingEffect } from '../api/api-utils'
import { RoleSelector } from './AdminUserDetail'

/** creates a new admin user */
const CreateUserModal = ({ onDismiss, portals, userCreated }:
                           { onDismiss: () => void,
                             portals: Portal[], userCreated: () => void
                           }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [portalShortcode, setPortalShortcode] =
    useState<string | null>(portals.length > 0 ? portals[0].shortcode : null)
  const { user } = useUser()
  const [newUser, setNewUser] = useState<NewAdminUser>({
    username: '',
    superuser: false,
    portalShortcode,
    roleNames: ['study_admin']
  })
  const portalOpts = portals.map(portal => ({ label: portal.name, value: portal.shortcode }))
  const selectedPortalOpt = portalOpts.find(portalOpt => portalOpt.value === portalShortcode)
  const [roles, setRoles] = useState<Role[]>([])

  useLoadingEffect(async () => {
    const fetchedRoles = await Api.fetchRoles()
    setRoles(fetchedRoles)
  })

  const createUser = async () => {
    doApiLoad(async () => {
      let createdUser: AdminUser
      if (!newUser.superuser) {
        createdUser = await Api.createPortalUser(newUser)
      } else {
        createdUser = await Api.createSuperuser(newUser)
      }
      Store.addNotification(successNotification(`${createdUser.username} created`))
      userCreated()
      onDismiss()
    }, { setIsLoading })
  }
  // username must be email-like, and either be a superuser or associated with a Portal (we're not yet supporting
  // mutliselect for users spanning multiple portals)
  const isUserValid = /^\S+@\S+\.\S+$/.test(newUser.username) && (newUser.superuser || newUser.portalShortcode)

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Add admin user</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <div className="mb-3">
            <label className="form-label">
              Email
              <input type="email" value={newUser.username} className="form-control"
                onChange={e => setNewUser({ ...newUser, username: e.target.value })}/>
              <span className="form-text">Email must be a Microsoft- or Google-based account</span>
            </label>
          </div>
          {user?.superuser && <div className="mb-3">
            <span>Superuser</span><br/>
            <label className="me-3">
              <input type="radio" name="superuser" value="true" checked={newUser.superuser}
                onChange={() => setNewUser({ ...newUser, portalShortcode: null, superuser: true })}
                className="me-1"/> Yes
            </label>
            <label>
              <input type="radio" name="superuser" value="false" checked={!newUser.superuser} className="me-1"
                onChange={() => setNewUser({ ...newUser, portalShortcode: null, superuser: false })}/> No
            </label>
          </div> }
          { !newUser.superuser && <div>
            <div>
              <label className="w-100 mb-3">
                Portal
                <Select options={portalOpts} value={selectedPortalOpt}
                  onChange={opt => setPortalShortcode(opt?.value ?? null)}/>
              </label>
            </div>
            <div>
              <RoleSelector roles={roles} selectedRoleNames={newUser.roleNames} setSelectedRoleNames={roleNames =>
                setNewUser({ ...newUser, roleNames })}/>
            </div>
          </div> }
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={createUser} disabled={!isUserValid}>Create</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateUserModal
