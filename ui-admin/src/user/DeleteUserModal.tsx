import React, { useState } from 'react'
import { Store } from 'react-notifications-component'
import { Modal } from 'react-bootstrap'
import Api, { Portal } from 'api/api'
import { AdminUser } from 'api/adminUser'
import { successNotification } from 'util/notifications'
import { doApiLoad } from '../api/api-utils'

/** Modal to remove a user from a portal. */
const DeleteUserModal = ({ subjUser, portal, userDeleted, onDismiss }:
                           {subjUser: AdminUser, portal?: Portal, userDeleted: () => void, onDismiss: () => void}) => {
  const [usernameConfirm, setUsernameConfirm] = useState('')
  const removeString = `remove ${subjUser.username}`
  const canRemove = usernameConfirm === removeString

  const doRemove = async () => {
    doApiLoad(async () => {
      if (portal) {
        await Api.removePortalUser(subjUser, portal.shortcode)
      } else {
        await Api.removeAdminUser(subjUser)
      }
      Store.addNotification(successNotification(`${subjUser.username} removed`))
      userDeleted()
      onDismiss()
    }, { customErrorMsg: 'Error removing user' })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Remove user</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <h3 className="h5">Remove user {portal ? ' from portal' : ''}: {subjUser.username}</h3>
        <div className="my-3">
          <label>
          Confirm by typing &quot;{removeString}&quot; below<br/>
            <input type="text" className="form-control" value={usernameConfirm}
              onChange={e => setUsernameConfirm(e.target.value)}/>
          </label>
        </div>
        <button type="button" className="btn btn-primary" onClick={doRemove} disabled={!canRemove}>Remove</button>
      </form>
    </Modal.Body>
  </Modal>
}

export default DeleteUserModal
