import Modal from 'react-bootstrap/Modal'

import React from 'react'

import { ModalProps } from 'react-bootstrap'
import { useApiContext } from 'src/participant/ApiProvider'
import { StudyEnvParams } from 'src/types/study'
import { ParticipantFile } from 'src/types/participantFile'
import { saveBlobAsDownload } from 'src/util/downloadUtils'

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
        Virus scanning is incomplete for this file.
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      We cannot verify if this file is safe to download. Please come back later or download now at your own risk.
    </Modal.Body>
    <Modal.Footer>
      <div className={'d-flex w-100'}>
        <button
          onClick={downloadAnyway}
          className='btn btn-primary mx-2'
        >
          Download Anyway
        </button>

        <button
          onClick={onClose}
          className='btn btn-outline-secondary mx-2'
        >
          Close
        </button>

      </div>
    </Modal.Footer>
  </ModalComponent>
}
