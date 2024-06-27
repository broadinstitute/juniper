import React from 'react'
import { Modal } from 'react-bootstrap'

/**
 *
 */
export const ConfirmationModal = (
  {
    title,
    body,
    onConfirm,
    onCancel,
    confirmButtonStyle = 'btn-success',
    confirmButtonContent = 'Confirm'
  } : {
    title: React.ReactNode,
    body: React.ReactNode,
    onConfirm: () => void,
    onCancel: () => void,
    confirmButtonStyle?: string,
    confirmButtonContent?: React.ReactNode
  }
) => {
  return <Modal show={true} onHide={onCancel}>
    <Modal.Header>
      <Modal.Title>{title}</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      {body}
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      <button className={`btn ${confirmButtonStyle}`}  onClick={onConfirm}>{confirmButtonContent}</button>
    </Modal.Footer>
  </Modal>
}
