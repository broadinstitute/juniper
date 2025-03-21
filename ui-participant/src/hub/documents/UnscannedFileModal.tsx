import Modal from 'react-bootstrap/Modal'

import React from 'react'
import Api from 'api/api'
import {
  Enrollee,
  ParticipantFile,
  saveBlobAsDownload,
  StudyEnvParams
} from '@juniper/ui-core'
import ThemedModal from 'components/ThemedModal'

export const UnscannedFileModal = ({
  studyEnvParams,
  enrollee,
  participantFile,
  onClose
} : {
    studyEnvParams: StudyEnvParams,
    enrollee: Enrollee,
    participantFile: ParticipantFile,
    onClose: () => void
}) => {
  const downloadAnyway = async () => {
    const response = await Api.downloadParticipantFile({
      studyEnvParams, enrolleeShortcode: enrollee.shortcode, fileName: participantFile.fileName
    })
    saveBlobAsDownload(await response.blob(), participantFile.fileName)
  }

  return <ThemedModal show={true} onHide={onClose}>
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
  </ThemedModal>
}
