import {
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalProps,
  ModalTitle
} from 'react-bootstrap'
import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { useI18n } from 'src/participant/I18nProvider'

export const QuarantinedFileModal = ({
  onClose,
  ModalComponent = Modal
} : {
  onClose: () => void,
  ModalComponent?: React.ElementType<ModalProps>
}) => {
  const { i18n } = useI18n()

  return <ModalComponent show={true} onHide={onClose}>
    <ModalHeader>
      <ModalTitle>
        {i18n('virusDetected')}
      </ModalTitle>
    </ModalHeader>
    <ModalBody>
      {i18n('fileQuarantinedCannotDownload')}
    </ModalBody>
    <ModalFooter>
      <div className={'d-flex w-100'}>
        <button
          onClick={onClose}
          className='btn btn-outline-secondary mx-2'
        >
          {i18n('goBack')}
        </button>
      </div>
    </ModalFooter>
  </ModalComponent>
}
