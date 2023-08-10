import React from 'react'
import Modal from 'react-bootstrap/Modal'

/**
 * Shown to the user if the form editor is opened and we detect a local draft.
 */
const LoadedLocalDraftModal = ({ onDismiss, lastUpdated }: {
  onDismiss: () => void
  lastUpdated: number
}) => {
  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>Survey Draft Loaded</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>A previously unsaved survey draft was automatically loaded.
        Be sure to save the survey in order to publish your changes.</p>
      <span>This draft was last updated {new Date(lastUpdated).toLocaleString()}</span>
    </Modal.Body>
  </Modal>
}

export default LoadedLocalDraftModal
