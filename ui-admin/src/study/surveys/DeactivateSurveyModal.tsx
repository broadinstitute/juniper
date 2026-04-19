import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import { StudyEnvironmentSurveyNamed } from '@juniper/ui-core'

/** renders a modal that allows deactivating a survey, with an option to also cancel existing participant tasks */
const DeactivateSurveyModal = ({
  studyEnvContext, selectedSurveyConfig, onDismiss
}: {
  studyEnvContext: StudyEnvContextT, selectedSurveyConfig: StudyEnvironmentSurveyNamed, onDismiss: () => void
}) => {
  const [isLoading, setIsLoading] = useState(false)
  const portalContext = useContext(PortalContext) as PortalContextT

  const deactivate = async (cancelTasks: boolean) => {
    setIsLoading(true)
    await Api.deactivateConfiguredSurvey(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      selectedSurveyConfig.envName,
      selectedSurveyConfig.id,
      cancelTasks
    ).catch(() =>
      Store.addNotification(failureNotification('Error deactivating survey'))
    )
    await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    setIsLoading(false)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Deactivate Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {selectedSurveyConfig.envName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <p>
        Are you sure you want to deactivate the <strong>{selectedSurveyConfig.survey.name}</strong> survey
        from the {selectedSurveyConfig.envName} environment?
      </p>
      <p>
        Existing participant responses will be preserved. You may also cancel any outstanding
        participant tasks for this survey.
      </p>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-danger" onClick={() => deactivate(true)}>
          Deactivate and cancel existing tasks
        </button>
        <button className="btn btn-warning" onClick={() => deactivate(false)}>
          Deactivate
        </button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default DeactivateSurveyModal
