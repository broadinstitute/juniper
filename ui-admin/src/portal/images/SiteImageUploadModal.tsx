import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { SiteImageMetadata } from 'api/api'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Button } from 'components/forms/Button'
import { LoadedPortalContextT } from '../PortalProvider'
import { useFileUploadButton } from 'util/uploadUtils'


export const allowedImageTypes = ['gif', 'ico', 'jpeg', 'jpg', 'png', 'svg', 'webp']
export const allowedDocumentTypes = ['pdf', 'json']
export const allowedTextTypes = ['csv', 'plain']
const FILE_TYPE_REGEX = new RegExp(
  [
    `^(?:image\\/(${allowedImageTypes.join('|')}))`,
    `(?:application\\/(${allowedDocumentTypes.join('|')}))`,
    `(?:text\\/(${allowedTextTypes.join('|')}))$`
  ].join('|')
)
/** Renders a modal for an admin to submit a sample collection kit request. */
export default function SiteImageUploadModal({
  portalContext,
  onDismiss, onSubmit,
  existingImage
}: {
    portalContext: LoadedPortalContextT,
    onDismiss: () => void,
    existingImage?: SiteImageMetadata,
    onSubmit: () => void }) {
  const [isLoading, setIsLoading] = useState(false)

  const [fileName, setFileName] = useState(existingImage?.cleanFileName ?? '')

  const handleFileChange = (newFile: File) => {
    // only autofill the name field if this is a new image
    if (!existingImage) {
      setFileName(cleanFileName(newFile.name))
    }
  }

  const { file, FileChooser } = useFileUploadButton(handleFileChange)
  const uploadImage = () => {
    if (!file) { return }
    const version = existingImage?.version ? existingImage.version + 1 : 1
    doApiLoad(async () => {
      await Api.uploadPortalImage(portalContext.portal.shortcode, fileName, version, file)
      Store.addNotification(successNotification('file uploaded'))
      onSubmit()
    }, { setIsLoading })
  }

  const validationMessages = []
  if (file && !file.type.match(FILE_TYPE_REGEX)) {
    validationMessages.push('This file extension is not supported.')
  }
  if (file && file.size > 10 * 1024 * 1024) {
    validationMessages.push('File size exceeds the 10MB limit.')
  }
  const enableSubmit = file && fileName && !validationMessages.length

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>{existingImage ? 'Update' : 'Upload'} media</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          <p>Supported extensions are {[...allowedImageTypes, ...allowedDocumentTypes].join(', ')}.
            Maximum size is 10MB</p>
          File:
          <div>
            { FileChooser }
            <span className="text-muted fst-italic ms-2">{file?.name}</span>
          </div>
          { validationMessages.map(msg => <div className='text-danger'>{msg}</div>)}
        </div>
        <label className="mt-3 mb-2 form-label">
                    Name:
          <input type="text" className="form-control" readOnly={!!existingImage}
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
 * See SiteImageService.java for the server-side implementation of this
 */
export const cleanFileName = (fileName: string) => {
  return fileName.toLowerCase()
    .replaceAll(/\s/g, '_')
    .replaceAll(/[^a-z\d._-]/g, '')
}
