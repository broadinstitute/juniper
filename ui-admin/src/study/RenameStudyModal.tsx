import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import { doApiLoad } from 'api/api-utils'
import Api, { Portal, Study } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'

/**
 *
 */
const RenameStudyModal = ({ portal, study, onClose }: { portal: Portal, study: Study, onClose: () => void }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [newName, setNewName] = useState('')

  const renameStudy = async () => {
    setIsLoading(true)

    await doApiLoad(async () => Api.renameStudy(portal.shortcode, study.shortcode, newName),
      { setIsLoading })
    onClose()
  }

  return <Modal show={true} onHide={onClose}>
    <Modal.Header closeButton>
      <Modal.Title>Rename Study</Modal.Title>
      <div className="ms-4">
        {portal.name}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
                Are you sure you want to rename the <strong>{study.name}</strong> study?
                This will change the study name across all environments.
      </div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
                    New name<br/>
          <input type="text" size={50} className="form-control" id="inputStudyName" value={newName}
            onChange={event => setNewName(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-danger"
          disabled={!newName}
          onClick={renameStudy}
        >Rename study</button>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default RenameStudyModal
