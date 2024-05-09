import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api from 'api/api'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Button } from 'components/forms/Button'
import { useFileUploadButton } from 'util/uploadUtils'
import { EnvironmentName } from '@juniper/ui-core'


export const allowedFileTypes = ['csv', 'tsv']
const FILE_TYPE_REGEX = new RegExp(`\\.(${allowedFileTypes.join('|')})$`)

/** Renders a modal for an admin to upload a data import file. */
export default function AddDataImportModal({
  portalShortcode, studyShortcode, envName,
  onDismiss, onSubmit
}: {
  portalShortcode: string,
  studyShortcode: string,
  envName: EnvironmentName,
  onDismiss: () => void,
  onSubmit: () => void
}) {
  const [isLoading, setIsLoading] = useState(false)

  const [fileName, setFileName] = useState('')

  const handleFileChange = (newFile: File) => {
    setFileName(newFile.name)
  }

  const { file, FileChooser } = useFileUploadButton(handleFileChange)
  const uploadDataFile = () => {
    if (!file) {
      return
    }
    doApiLoad(async () => {
      await Api.uploadDataImport(file, portalShortcode, studyShortcode, envName)
      Store.addNotification(successNotification('data import file loaded'))
      onSubmit()
    }, { setIsLoading })
  }

  const validationMessages = []
  if (file && !file.name.match(FILE_TYPE_REGEX)) {
    validationMessages.push('This file extension is not supported.')
  }
  const enableSubmit = file && fileName && !validationMessages.length

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Upload data import</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          <p>Supported extensions are {allowedFileTypes.join(', ')}.</p>
          File:
          <div>
            {FileChooser}
            <span className="text-muted fst-italic ms-2">{file?.name}</span>
          </div>
          {validationMessages.map((msg, index) => <div key={index} className='text-danger'>{msg}</div>)}
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
        <Button variant="primary" disabled={!enableSubmit} onClick={uploadDataFile}>Upload</Button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
