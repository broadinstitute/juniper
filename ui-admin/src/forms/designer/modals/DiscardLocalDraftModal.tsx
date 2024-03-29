import { Modal, ModalFooter } from 'react-bootstrap'
import { Button } from 'components/forms/Button'
import React from 'react'
import { deleteDraft } from '../utils/formDraftUtils'

/**
 * Modal presenting the user with the option to discard a local draft. Shown on Cancel if there is a local draft.
 */
const DiscardLocalDraftModal = ({ formDraftKey, onExit, onSaveDraft, onDismiss }: {
  formDraftKey: string
  onExit: () => void
  onSaveDraft: () => void
  onDismiss: () => void
}) => {
  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>Unsaved Changes</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>Are you sure you want to cancel? You have an unsaved survey draft. You can save the
        draft and return to edit it later, or discard the draft if it is no longer needed.</p>
    </Modal.Body>
    <ModalFooter>
      <Button
        variant="primary"
        onClick={() => {
          onSaveDraft()
          onDismiss()
          onExit()
        }}
      >
        Exit & save draft
      </Button>
      <Button
        variant="danger"
        onClick={() => {
          deleteDraft({ formDraftKey })
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
