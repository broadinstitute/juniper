import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from '../../../api/api'

const CreateDatasetModal = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT, show: boolean,
    setShow:  React.Dispatch<React.SetStateAction<boolean>>}) => {
  const [isLoading, setIsLoading] = useState(false)
  const [datasetName, setDatasetName] = useState('')
  const createDataset = () => {
    setIsLoading(true)
    Api.createDatasetForStudyEnvironment(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, { name: datasetName })
    setShow(false)
    setIsLoading(false)
    setDatasetName('')
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
        <label className="form-label">
              Dataset Name <input type="text" className="form-control" id="inputDatasetName" value={datasetName}
            onChange={event => setDatasetName(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={createDataset}>Create</button>
        <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateDatasetModal
