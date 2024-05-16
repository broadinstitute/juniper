import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { NavbarItem } from '@juniper/ui-core'

/** renders a modal for deleting pages from the portal website */
const DeleteNavItemModal = ({ navItem, deleteNavItem, onDismiss }: {
  navItem: NavbarItem, deleteNavItem: (navItemText: string) => void, onDismiss: () => void
}) => {
  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Delete Page</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Are you sure you want to delete <strong>{navItem.text}</strong> from the navigation bar?
        {(navItem.itemType === 'INTERNAL') && <div className="mt-2">
          This will also delete the page <strong>{navItem.htmlPage.title}</strong>
        </div>}
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={() => {
          deleteNavItem(navItem.text)
          onDismiss()
        }}
      >Delete</button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default DeleteNavItemModal
