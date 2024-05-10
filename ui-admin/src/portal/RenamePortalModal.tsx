import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import { doApiLoad } from 'api/api-utils'
import Api, { Portal } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'

/**
 *
 */
const RenamePortalModal = ({ portal, onClose }: { portal: Portal; onClose: () => void }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [newName, setNewName] = useState('')

  const renamePortal = async () => {
    setIsLoading(true)

    await doApiLoad(async () => Api.renamePortal(portal.shortcode, newName),
      { setIsLoading })
    onClose()
  }

  return <Modal show={true} onHide={onClose}>
    <Modal.Header closeButton>
      <Modal.Title>Rename Portal</Modal.Title>
      <div className="ms-4">
        {portal.name}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
            Are you sure you want to rename the <strong>{portal.name}</strong> portal?
            This will change the portal name across all environments and studies.
      </div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
          New name<br/>
          <input type="text" size={50} className="form-control" id="inputPortalName" value={newName}
            onChange={event => setNewName(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-danger"
          disabled={!newName}
          onClick={renamePortal}
        >Rename portal</button>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default RenamePortalModal
