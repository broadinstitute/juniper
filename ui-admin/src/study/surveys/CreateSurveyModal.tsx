import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
const CreateSurveyModal = ({ studyEnvContext, show, setShow }: {studyEnvContext: StudyEnvContextT,
    show: boolean, setShow:  React.Dispatch<React.SetStateAction<boolean>> }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [surveyName, setSurveyName] = useState('')
  const [surveyStableId, setSurveyStableId] = useState('')
  const createDataset = async () => {
    setIsLoading(true)
    const response = await Api.createNewSurvey(studyEnvContext.portal.shortcode,
      {content: "", createdAt: 0, footer: "", id: "", lastUpdatedAt: 0,
        version: 0, name: surveyName, stableId: surveyStableId })
    setShow(false)
    setIsLoading(false)
    clearFields()
  }
  const clearFields = () => {
    setSurveyName('')
    setSurveyStableId('')
  }

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>Create New Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label"> Survey Name
          <input type="text" size={50} className="form-control" id="inputSurveyName" value={surveyName}
            onChange={event => setSurveyName(event.target.value)}/>
        </label>
        <label className="form-label"> Survey Stable ID
          <input type="text" size={50} className="form-control" id="inputSurveyStableId" value={surveyStableId}
            onChange={event => setSurveyStableId(event.target.value)}/>
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

export default CreateSurveyModal
