import { faEdit, faTimes } from '@fortawesome/free-solid-svg-icons'
import React, { useState } from 'react'
import { Row } from '@tanstack/react-table'
import { Portal } from 'api/api'
import { AdminUser } from 'api/adminUser'
import DeleteUserModal from './DeleteUserModal'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import EditUserModal from './EditPortalUserModal'
import { Button } from '../components/forms/Button'

/** Handle actions for a given portal user. */
const userAction = ({ row, portal, onUserListChanged }:
                      {row: Row<AdminUser>, portal?: Portal, onUserListChanged: () => void}) => {
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const enableEditing = !row.original.superuser && portal
  return <div>
    { enableEditing && <Button variant="secondary" onClick={() => setShowEditModal(true)}>
      <FontAwesomeIcon icon={faEdit}/>
    </Button> }
    <button className="btn-secondary btn" onClick={() => setShowDeleteModal(true)}>
      <FontAwesomeIcon icon={faTimes}/>
    </button>
    { showDeleteModal &&
      <DeleteUserModal subjUser={row.original} portal={portal} onDismiss={() => setShowDeleteModal(false)}
        userDeleted={onUserListChanged}/>}
    { showEditModal && <EditUserModal userId={row.original.id}
      portalShortcode={portal!.shortcode}
      onDismiss={() => setShowEditModal(false)}
      userUpdated={onUserListChanged}/>}
  </div>
}

export default userAction
