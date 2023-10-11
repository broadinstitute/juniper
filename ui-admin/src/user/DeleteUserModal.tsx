import React, { useState } from 'react'
import { Store } from 'react-notifications-component'
import Api, { AdminUser, Portal } from 'api/api'
import { failureNotification, successNotification } from 'util/notifications'

/** Modal to remove a user from a portal. */
const DeleteUserModal = ({ subjUser, portal, userDeleted, onDismiss }:
                           {subjUser: AdminUser, portal: Portal, userDeleted: () => void, onDismiss: () => void}) => {
  const [usernameConfirm, setUsernameConfirm] = useState('')
  const removeString = `remove ${subjUser.username}`
  const canRemove = usernameConfirm === removeString

  const doRemove = async () => {
      await doApiLoad(() => {
         Api.removePortalUser(subjUser, portal.shortcode)
         Store.addNotification(successNotification(`${subjUser.username} removed`))
         userDeleted()
         onDismiss()
    }, {customErrorMsg: 'Error removing user'} )
  }

  return <div className="p4">
    <form onSubmit={e => e.preventDefault()}>
      <h3 className="h5">Remove user from portal: {subjUser.username}</h3>
      <div className="my-3">
        <label>
          Confirm by typing &quot;{removeString}&quot; below<br/>
          <strong>Removing a user is permanent</strong>
          <input type="text" className="form-control" value={usernameConfirm}
            onChange={e => setUsernameConfirm(e.target.value)}/>
        </label>
      </div>
      <button type="button" className="btn btn-primary" onClick={doRemove} disabled={!canRemove}>Remove user</button>
    </form>
  </div>
}

export default DeleteUserModal
