import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { NavbarItem } from '@juniper/ui-core'

/** renders a modal for renaming pages of the portal website */
const RenameNavItemModal = ({ navItem, renameNavItem, onDismiss }: {
  navItem: NavbarItem, renameNavItem: (navItemText: string) => void, onDismiss: () => void
}) => {
  const [newName, setNewName] = useState(navItem.text)

  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Rename Page</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        <label htmlFor="renameNavItemInput">New name for <strong>{navItem.text}</strong>:</label>
        <input
          type="text"
          id="renameNavItemInput"
          className="form-control"
          value={newName}
          onChange={e => {
            setNewName(e.target.value)
          }}
        />
        <label htmlFor="renameNavItemPathInput">New path for <strong>{navItem.text}</strong>:</label>
        <input
          type="text"
          id="renameNavItemPathInput"
          className="form-control"
          value={newName}
          onChange={e => {
            setNewName(e.target.value)
          }}
        />
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={() => {
          renameNavItem(newName)
          onDismiss()
        }}
      >Rename
      </button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default RenameNavItemModal
