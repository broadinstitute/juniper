import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { useNavigate } from 'react-router-dom'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
const CreateSurveyModal = ({ studyEnvContext, isReadOnlyEnv, show, setShow }: {studyEnvContext: StudyEnvContextT,
  isReadOnlyEnv: boolean, show: boolean, setShow:  React.Dispatch<React.SetStateAction<boolean>> }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [surveyName, setSurveyName] = useState('')
  const [surveyStableId, setSurveyStableId] = useState('')

  const navigate = useNavigate()

  const createSurvey = async () => {
    setIsLoading(true)
    await Api.createNewSurvey(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      {
        createdAt: 0, id: '', lastUpdatedAt: 0, version: 1,
        content: '{"pages":[]}', name: surveyName, stableId: surveyStableId
      }).catch(e =>
      Store.addNotification(failureNotification(`Error creating survey: ${e.message}`))
    )
    setShow(false)
    //TODO: this requires a full refresh of the portal context to work. for now, just refresh the page after it errors
    navigate(`surveys/${surveyStableId}?readOnly=${isReadOnlyEnv}`)
    setIsLoading(false)
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
        <button className="btn btn-primary" onClick={createSurvey}>Create</button>
        <button className="btn btn-secondary" onClick={() => {
          setShow(false)
          clearFields()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateSurveyModal
