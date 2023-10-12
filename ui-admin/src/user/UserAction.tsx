import { faTimes } from '@fortawesome/free-solid-svg-icons'
import React, { useState } from 'react'
import { Row } from '@tanstack/react-table'
import { AdminUser, Portal } from 'api/api'
import DeleteUserModal from './DeleteUserModal'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

/** Handle actions for a given portal user. */
const userAction = ({ row, portal, onUserListChanged }:
                      {row: Row<AdminUser>, portal: Portal, onUserListChanged: () => void}) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false)

  return <div>
    <button className="btn-secondary btn" onClick={() => setShowDeleteModal(true)}>
      <FontAwesomeIcon icon={faTimes}/>
    </button>
    {showDeleteModal &&
      <DeleteUserModal subjUser={row.original} portal={portal} onDismiss={() => setShowDeleteModal(false)}
        userDeleted={onUserListChanged}/>}
  </div>
}

export default userAction
