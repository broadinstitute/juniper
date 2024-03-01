import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { SiteMediaMetadata } from 'api/api'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Button } from 'components/forms/Button'
import { LoadedPortalContextT } from '../PortalProvider'

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function SiteMediaDeleteModal({
  portalContext,
  onDismiss,
  onSubmit,
  media
}: {
    portalContext: LoadedPortalContextT,
    onDismiss: () => void,
    media: SiteMediaMetadata,
    onSubmit: () => void }) {
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const deleteImage = () => {
    doApiLoad(async () => {
      await Api.deletePortalMedia(portalContext.portal.shortcode, media.id)
      Store.addNotification(successNotification('file successfully deleted'))
      onSubmit()
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Delete Media</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      Are you sure you want to delete <span className='fst-italic'>{media.cleanFileName}</span>?
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
        <Button variant="danger" onClick={deleteImage}>Delete</Button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
