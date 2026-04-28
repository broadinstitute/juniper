import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import { StudyEnvironmentSurveyNamed } from '@juniper/ui-core'

/** renders a modal that cancels existing participant tasks for a survey */
const CancelSurveyTasksModal = ({
  studyEnvContext, selectedSurveyConfig, onDismiss
}: {
  studyEnvContext: StudyEnvContextT, selectedSurveyConfig: StudyEnvironmentSurveyNamed, onDismiss: () => void
}) => {
  const [isLoading, setIsLoading] = useState(false)
  const portalContext = useContext(PortalContext) as PortalContextT

  const cancelTasks = async () => {
    setIsLoading(true)
    await Api.cancelConfiguredSurveyTasks(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      selectedSurveyConfig.envName,
      selectedSurveyConfig.id
    ).catch(() =>
      Store.addNotification(failureNotification('Error cancelling tasks'))
    )
    await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    setIsLoading(false)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Cancel Existing Tasks</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {selectedSurveyConfig.envName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <p>
        Are you sure you want to cancel all outstanding participant tasks for
        the <strong>{selectedSurveyConfig.survey.name}</strong> survey
        in the {selectedSurveyConfig.envName} environment?
      </p>
      <p>
        Existing participant responses will be preserved.
      </p>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-danger" onClick={cancelTasks}>
          Cancel existing tasks
        </button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CancelSurveyTasksModal
