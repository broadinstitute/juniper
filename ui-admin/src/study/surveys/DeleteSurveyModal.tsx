import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { StudyEnvironmentSurvey } from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'

/** renders a modal that allows deleting a survey */
const DeleteSurveyModal = ({
  studyEnvContext, selectedSurveyConfig, onDismiss
}: {
  studyEnvContext: StudyEnvContextT, selectedSurveyConfig: StudyEnvironmentSurvey, onDismiss: () => void}) => {
  const [isLoading, setIsLoading] = useState(false)

  const portalContext = useContext(PortalContext) as PortalContextT

  const [confirmDeleteSurvey, setConfirmDeleteSurvey] = useState('')
  const deleteString = `delete ${selectedSurveyConfig.survey.name}`
  const canDelete = confirmDeleteSurvey.toLowerCase() === deleteString.toLowerCase()

  const deleteSurvey = async () => {
    setIsLoading(true)

    await Api.deleteSurvey(studyEnvContext.portal.shortcode,
      selectedSurveyConfig.survey.stableId
    ).catch(() =>
      Store.addNotification(failureNotification('Error deleting survey'))
    )
    await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    setIsLoading(false)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Delete Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Are you sure you want to delete the <strong>{selectedSurveyConfig.survey.name}</strong> survey? You
        cannot delete a survey if it is in use by an IRB or live study environment.</div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
          Confirm by typing &quot;{deleteString}&quot; below.<br/>
          <input type="text" size={50} className="form-control" id="inputSurveyRemoval" value={confirmDeleteSurvey}
            onChange={event => setConfirmDeleteSurvey(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-danger"
          disabled={!canDelete}
          onClick={deleteSurvey}
        >Delete survey</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default DeleteSurveyModal
