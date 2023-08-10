import { Modal, ModalFooter } from 'react-bootstrap'
import { Button } from '../../components/forms/Button'
import React from 'react'

/**
 * Modal presenting the user with the option to discard a local draft. Shown on Cancel if there is a local draft.
 */
const DiscardLocalDraftModal = ({ formDraftKey, onExit, onDismiss }: {
  formDraftKey: string
  onExit: () => void
  onDismiss: () => void
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
        onClick={() => {
          onDismiss()
          onExit()
        }}
      >
        Exit & save draft
      </Button>
      <Button
        variant="danger"
        onClick={() => {
          localStorage.removeItem(formDraftKey)
          onDismiss()
          onExit()
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
