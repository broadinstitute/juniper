import {
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalTitle
} from 'react-bootstrap'
import React from 'react'
import ThemedModal from 'components/ThemedModal'

export const QuarantinedFileModal = ({
  onClose
} : {
  onClose: () => void
}) => {
  return <ThemedModal show={true} onHide={onClose}>
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
  </ThemedModal>
}
