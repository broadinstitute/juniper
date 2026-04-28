import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import { StudyEnvironmentSurveyNamed } from '@juniper/ui-core'

/** renders a modal that allows re-activating a deactivated survey */
const ActivateSurveyModal = ({
  studyEnvContext, selectedSurveyConfig, onDismiss
}: {
  studyEnvContext: StudyEnvContextT, selectedSurveyConfig: StudyEnvironmentSurveyNamed, onDismiss: () => void
}) => {
  const [isLoading, setIsLoading] = useState(false)
  const portalContext = useContext(PortalContext) as PortalContextT

  const activate = async () => {
    setIsLoading(true)
    await Api.activateConfiguredSurvey(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      selectedSurveyConfig.envName,
      selectedSurveyConfig.id
    ).catch(() =>
      Store.addNotification(failureNotification('Error activating survey'))
    )
    await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    setIsLoading(false)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Re-activate Survey</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      Re-activate this survey for {selectedSurveyConfig.envName}?
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={activate}>Ok</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default ActivateSurveyModal
