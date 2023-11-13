import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { HtmlPage } from '@juniper/ui-core'

/** renders a modal allows the user to delete a page from the portal website */
const DeletePageModal = ({ renderedPage, deletePage, onDismiss }: {
  renderedPage: HtmlPage, deletePage: (page: HtmlPage) => void, onDismiss: () => void
}) => {
  const [confirmDeletePage, setConfirmDeletePage] = useState('')
  const deleteString = `delete ${renderedPage.title}`

  return <Modal show={true}
    onHide={() => {
      onDismiss()
      setConfirmDeletePage('')
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Delete Page</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Are you sure you want to delete the <strong>{renderedPage.title}</strong> page? Deleted pages
        can be restored from the website version history.</div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
          Confirm by typing &quot;{deleteString}&quot; below.<br/>
          <input type="text" size={50} className="form-control" id="inputPageRemoval" value={confirmDeletePage}
            onChange={event => setConfirmDeletePage(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        disabled={confirmDeletePage.toLowerCase() !== deleteString.toLowerCase()}
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
