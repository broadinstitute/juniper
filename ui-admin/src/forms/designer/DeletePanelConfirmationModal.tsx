import React from 'react'
import { Modal, ModalFooter } from 'react-bootstrap'

import { FormPanel } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'

type DeletePanelConfirmationModalProps = {
  panel: FormPanel
  onConfirm: (response: { deleteContents: boolean }) => void
  onDismiss: () => void
}

/** Modal to confirm deleting a panel from a page. */
export const DeletePanelConfirmationModal = (props: DeletePanelConfirmationModalProps) => {
  const { panel, onConfirm, onDismiss } = props

  return (
    <Modal show onHide={onDismiss}>
      <Modal.Header>Delete panel contents?</Modal.Header>
      <Modal.Body>
        <p>This panel contains {panel.elements.length} element{panel.elements.length !== 1 && 's'}.</p>
        <ol>
          {panel.elements.map(el => {
            return <li key={el.name}>{el.name}</li>
          })}
        </ol>
        <p>Do you want to delete these elements with the panel or keep them on the page?</p>
      </Modal.Body>
      <ModalFooter>
        <Button
          variant="danger"
          onClick={() => {
            onConfirm({ deleteContents: true })
          }}
        >
          Delete contents
        </Button>
        <Button
          variant="primary"
          onClick={() => {
            onConfirm({ deleteContents: false })
          }}
        >
          Keep contents
        </Button>
        <Button
          variant="secondary"
          onClick={onDismiss}
        >
          Cancel
        </Button>
      </ModalFooter>
    </Modal>
  )
}
