import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

const CreateDatasetModal = ({ studyEnvContext, show, setShow, loadDatasets }: {studyEnvContext: StudyEnvContextT,
  show: boolean, setShow:  React.Dispatch<React.SetStateAction<boolean>>, loadDatasets: () => void }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [datasetName, setDatasetName] = useState('')
  const [datasetDescription, setDatasetDescription] = useState('')
  const createDataset = async () => {
    setIsLoading(true)
    const response = await Api.createDatasetForStudyEnvironment(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName,
      { name: datasetName, description: datasetDescription })
    if (response.ok) {
      Store.addNotification(successNotification(`${datasetName} created`))
    } else {
      Store.addNotification(failureNotification(`${datasetName} creation failed`))
    }
    loadDatasets()
    setShow(false)
    setIsLoading(false)
    clearFields()
  }
  const clearFields = () => {
    setDatasetName('')
    setDatasetDescription('')
  }

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>Create Dataset</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label"> Dataset Name
          <input type="text" size={50} className="form-control" id="inputDatasetName" value={datasetName}
            onChange={event => setDatasetName(event.target.value)}/>
        </label>
        <label className="form-label"> Description
          <textarea rows={3} cols={50} value={datasetDescription}
            onChange={event => setDatasetDescription(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={createDataset}>Create</button>
        <button className="btn btn-secondary" onClick={() => {
          setShow(false)
          clearFields()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateDatasetModal
