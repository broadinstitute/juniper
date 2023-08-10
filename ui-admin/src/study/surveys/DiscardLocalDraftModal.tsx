import { Modal, ModalFooter } from 'react-bootstrap'
import { Button } from '../../components/forms/Button'
import React from 'react'

/**
 *
 */
const DiscardLocalDraftModal = ({ onDismiss, onClose, onDiscard }: {
  onDismiss: () => void
  onClose: () => void
  onDiscard: () => void
}) => {
  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>Discard Changes?</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>Are you sure you want to cancel? You have an unsaved survey draft. You can
        discard the draft if it is no longer needed.</p>
    </Modal.Body>
    <ModalFooter>
      <Button
        variant="primary"
        onClick={onClose}
      >
        Exit & save draft
      </Button>
      <Button
        variant="danger"
        onClick={() => {
          onDiscard()
        }}
      >
        Exit & discard draft
      </Button>
      <Button
        variant="secondary"
        onClick={onDismiss}
      >
        Cancel
      </Button>
    </ModalFooter>
  </Modal>
}

export default DiscardLocalDraftModal
