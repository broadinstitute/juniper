import React from 'react'
import Modal from 'react-bootstrap/Modal'

/**
 *
 */
const LoadedLocalDraftModal = ({ onDismiss }: {
  onDismiss: () => void
}) => {
  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>Survey Draft Loaded</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>Previously unsaved changes to this survey have been automatically loaded.
        Be sure to save the survey in order to publish your changes.</p>
    </Modal.Body>
  </Modal>
}

export default LoadedLocalDraftModal
