import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'
import { Study } from '@juniper/ui-core/build/types/study'
import { Portal } from '@juniper/ui-core/build/types/portal'

/** renders a modal that allows deleting a survey */
const DeleteStudyModal = ({
  portal, study, onDismiss, reload
}: {
  portal: Portal, study:Study, onDismiss: () => void, reload: () => void}) => {
  const [isLoading, setIsLoading] = useState(false)
  const [confirmDeleteStudy, setConfirmDeleteStudy] = useState('')
  const deleteString = `delete ${study.name}`
  const canDelete = confirmDeleteStudy.toLowerCase() === deleteString.toLowerCase()

  const deleteStudy = async () => {
    setIsLoading(true)

    await Api.deleteStudy(portal.shortcode, study.shortcode).catch(() =>
      Store.addNotification(failureNotification('Error deleting study'))
    )
    reload()
    setIsLoading(false)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Delete Study</Modal.Title>
      <div className="ms-4">
        {study.name}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Are you sure you want to delete the <strong>{study.name}</strong> study? </div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
          Confirm by typing &quot;{deleteString}&quot; below.<br/>
          <input type="text" size={50} className="form-control" id="inputSurveyRemoval" value={confirmDeleteStudy}
            onChange={event => setConfirmDeleteStudy(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-danger"
          disabled={!canDelete}
          onClick={deleteStudy}
        >Delete study</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default DeleteStudyModal
