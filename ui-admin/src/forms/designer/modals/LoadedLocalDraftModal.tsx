import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'

/**
 * Shown to the user if the form editor is opened and we detect a local draft.
 */
const LoadedLocalDraftModal = ({ onDismiss, lastUpdated }: {
  onDismiss: () => void
  lastUpdated: number | undefined
}) => {
  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>Survey Draft Loaded</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>A previously unsaved survey draft was automatically loaded.
        Be sure to save the survey in order to publish your changes.</p>
      {lastUpdated && <span>This draft was last updated {new Date(lastUpdated).toLocaleString()}</span> }
    </Modal.Body>
    <Modal.Footer>
      <Button
        variant="primary"
        onClick={onDismiss}
      >
        Continue
      </Button>
    </Modal.Footer>
  </Modal>
}

export default LoadedLocalDraftModal
