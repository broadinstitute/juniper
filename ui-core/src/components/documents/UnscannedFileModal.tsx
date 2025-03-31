import Modal from 'react-bootstrap/Modal'

import React from 'react'

import { ModalProps } from 'react-bootstrap'
import { useApiContext } from '../../participant/ApiProvider'
import { StudyEnvParams } from '../../types/study'
import { ParticipantFile } from '../../types/participantFile'
import { saveBlobAsDownload } from '../../util/downloadUtils'
import { useI18n } from 'src/participant/I18nProvider'

export const UnscannedFileModal = ({
  studyEnvParams,
  enrolleeShortcode,
  participantFile,
  onClose,
  ModalComponent = Modal
} : {
  studyEnvParams: StudyEnvParams,
  enrolleeShortcode: string,
  participantFile: ParticipantFile,
  onClose: () => void,
  ModalComponent?: React.ElementType<ModalProps>
}) => {
  const { i18n } = useI18n()

  const Api = useApiContext()
  const downloadAnyway = async () => {
    const response = await Api.downloadParticipantFile({
      studyEnvParams, enrolleeShortcode, fileName: participantFile.fileName
    })
    saveBlobAsDownload(await response.blob(), participantFile.fileName)
  }

  return <ModalComponent show={true} onHide={onClose}>
    <Modal.Header>
      <Modal.Title>
        {i18n('virusScanningIncomplete')}
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      {i18n('virusScanningIncompleteCannotVerify')}
    </Modal.Body>
    <Modal.Footer>
      <div className={'d-flex w-100'}>
        <button
          onClick={downloadAnyway}
          className='btn btn-primary mx-2'
        >
          {i18n('downloadAnyway')}
        </button>

        <button
          onClick={onClose}
          className='btn btn-outline-secondary mx-2'
        >
          {i18n('goBack')}
        </button>
      </div>
    </Modal.Footer>
  </ModalComponent>
}
