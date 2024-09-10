import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { HtmlPage } from '@juniper/ui-core'

/** renders a modal for deleting pages from the portal website */
const DeleteNavItemModal = ({ page, deletePage, onDismiss }: {
  page: HtmlPage, deletePage: () => void, onDismiss: () => void
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
        Are you sure you want to delete <strong>{page.title}</strong> from your website?
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={() => {
          deletePage()
          onDismiss()
        }}
      >Delete</button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default DeleteNavItemModal
