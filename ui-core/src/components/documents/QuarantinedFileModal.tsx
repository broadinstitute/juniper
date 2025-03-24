import {
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalProps,
  ModalTitle
} from 'react-bootstrap'
import React from 'react'
import Modal from 'react-bootstrap/Modal'

export const QuarantinedFileModal = ({
  onClose,
  ModalComponent = Modal
} : {
  onClose: () => void,
  ModalComponent?: React.ElementType<ModalProps>
}) => {
  return <ModalComponent show={true} onHide={onClose}>
    <ModalHeader>
      <ModalTitle>
        A virus was detected within this file.
      </ModalTitle>
    </ModalHeader>
    <ModalBody>
      This file has been quarantined and cannot be downloaded.
      If you believe this is a mistake, please contact support.
    </ModalBody>
    <ModalFooter>
      <div className={'d-flex w-100'}>
        <button
          onClick={onClose}
          className='btn btn-outline-secondary mx-2'
        >
          Close
        </button>
      </div>
    </ModalFooter>
  </ModalComponent>
}
