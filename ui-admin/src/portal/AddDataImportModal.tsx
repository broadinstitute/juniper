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


export const allowedIFileTypes = ['csv', 'tsv']
export const allowedDocumentTypes = ['pdf', 'json']
export const allowedTextTypes = ['csv', 'txt']
export const allowedFileTypes = [...allowedIFileTypes, ...allowedDocumentTypes, ...allowedTextTypes]
const FILE_TYPE_REGEX = new RegExp(`\\.(${allowedFileTypes.join('|')})$`)

/** Renders a modal for an admin to submit a sample collection kit request. */
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
    setFileName(cleanFileName(newFile.name))
  }

  const { file, FileChooser } = useFileUploadButton(handleFileChange)
  const uploadDataFile = () => {
    if (!file) {
      return
    }
    //const version = existingMedia?.version ? existingMedia.version + 1 : 1
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
          <p>Supported extensions are {allowedIFileTypes.join(', ')}.</p>
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

/**
 * A cleanFileName is a portal-scoped-unique, URL-safe identifier.
 * cleanFileName is the upload filename with whitespace replaced with _,
 * stripped of special characters and whitespace except "." "_" or "-", then lowercased.
 * See SiteMediaService.java for the server-side implementation of this
 */
export const cleanFileName = (fileName: string) => {
  return fileName.toLowerCase()
    .replaceAll(/\s/g, '_')
    .replaceAll(/[^a-z\d._-]/g, '')
}
