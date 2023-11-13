import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { HtmlPage } from '@juniper/ui-core'

/** renders a modal for deleting pages from the portal website */
const DeletePageModal = ({ renderedPage, deletePage, onDismiss }: {
  renderedPage: HtmlPage, deletePage: (page: HtmlPage) => void, onDismiss: () => void
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
        Are you sure you want to delete the <strong>{renderedPage.title}</strong> page? Deleted pages
        can be restored from the website version history.
      </div>
      <div className="mb-3">
        Note that you must save your changes in the editor before your deletion will take effect.
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={() => {
          deletePage(renderedPage)
          onDismiss()
        }}
      >Delete</button>
      <button className="btn btn-secondary" onClick={() => {
        onDismiss()
      }}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default DeletePageModal
