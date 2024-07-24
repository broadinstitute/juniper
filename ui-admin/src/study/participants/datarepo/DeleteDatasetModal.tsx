import React, { useState } from 'react'
import { StudyEnvContextT, studyEnvDatasetListViewPath } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { useNavigate } from 'react-router-dom'
import Api from 'api/api'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

const DeleteDatasetModal = ({ studyEnvContext, datasetName, show, setShow, loadDatasets }: {
    studyEnvContext: StudyEnvContextT, datasetName: string, show: boolean,
    setShow:  React.Dispatch<React.SetStateAction<boolean>>, loadDatasets: () => void }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [confirmDatasetDeletion, setConfirmDatasetDeletion] = useState('')
  const deleteString = `delete ${datasetName}`
  const canDelete = confirmDatasetDeletion === deleteString
  const navigate = useNavigate()

  const deleteDataset = async () => {
    setIsLoading(true)
    const response = await Api.deleteDatasetForStudyEnvironment(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName, datasetName)
    if (response.ok) {
      Store.addNotification(successNotification(`Deletion of dataset ${datasetName} has been initiated`))
      navigate(studyEnvDatasetListViewPath(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName))
    } else {
      Store.addNotification(failureNotification(`${datasetName} deletion failed`))
    }
    loadDatasets()
    setIsLoading(false)
    setShow(false)
  }

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>Delete Dataset</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
                    Confirm by typing &quot;{deleteString}&quot; below.<br/>
          <strong>Deletion is permanent!</strong>
          <input type="text" size={50} className="form-control" id="inputDatasetDeletion" value={confirmDatasetDeletion}
            onChange={event => setConfirmDatasetDeletion(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={deleteDataset} disabled={!canDelete}>Delete</button>
        <button className="btn btn-secondary" onClick={() => {
          setShow(false)
          setConfirmDatasetDeletion('')
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default DeleteDatasetModal
