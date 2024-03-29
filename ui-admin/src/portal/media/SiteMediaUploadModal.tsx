import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { SiteMediaMetadata } from 'api/api'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Button } from 'components/forms/Button'
import { LoadedPortalContextT } from '../PortalProvider'
import { useFileUploadButton } from 'util/uploadUtils'


export const allowedImageTypes = ['gif', 'ico', 'jpeg', 'jpg', 'png', 'svg', 'webp']
export const allowedDocumentTypes = ['pdf', 'json']
export const allowedTextTypes = ['csv', 'txt']
export const allowedFileTypes = [...allowedImageTypes, ...allowedDocumentTypes, ...allowedTextTypes]
const FILE_TYPE_REGEX = new RegExp(`\\.(${allowedFileTypes.join('|')})$`)

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function SiteMediaUploadModal({
  portalContext,
  onDismiss, onSubmit,
  existingMedia
}: {
  portalContext: LoadedPortalContextT,
  onDismiss: () => void,
  existingMedia?: SiteMediaMetadata,
  onSubmit: () => void
}) {
  const [isLoading, setIsLoading] = useState(false)

  const [fileName, setFileName] = useState(existingMedia?.cleanFileName ?? '')

  const handleFileChange = (newFile: File) => {
    // only autofill the name field if this is a new image
    if (!existingMedia) {
      setFileName(cleanFileName(newFile.name))
    }
  }

  const { file, FileChooser } = useFileUploadButton(handleFileChange)
  const uploadImage = () => {
    if (!file) {
      return
    }
    const version = existingMedia?.version ? existingMedia.version + 1 : 1
    doApiLoad(async () => {
      await Api.uploadPortalMedia(portalContext.portal.shortcode, fileName, version, file)
      Store.addNotification(successNotification('file uploaded'))
      onSubmit()
    }, { setIsLoading })
  }

  const validationMessages = []
  if (file && !file.name.match(FILE_TYPE_REGEX)) {
    validationMessages.push('This file extension is not supported.')
  }
  if (file && file.size > 10 * 1024 * 1024) {
    validationMessages.push('File size exceeds the 10MB limit.')
  }
  const enableSubmit = file && fileName && !validationMessages.length

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>{existingMedia ? 'Update' : 'Upload'} media</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          <p>Supported extensions are {allowedFileTypes.join(', ')}.
            Maximum size is 10MB</p>
          File:
          <div>
            {FileChooser}
            <span className="text-muted fst-italic ms-2">{file?.name}</span>
          </div>
          {validationMessages.map((msg, index) => <div key={index} className='text-danger'>{msg}</div>)}
        </div>
        <label className="mt-3 mb-2 form-label">
          Name:
          <input type="text" className="form-control" readOnly={!!existingMedia}
            onChange={e => setFileName(cleanFileName(e.target.value))} value={fileName}/>
        </label>
        <p>File names must be lowercase,
          and cannot contain special characters other than dashes or underscores.</p>

      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
        <Button variant="primary" disabled={!enableSubmit} onClick={uploadImage}>Upload</Button>
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
