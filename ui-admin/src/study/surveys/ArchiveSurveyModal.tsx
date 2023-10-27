import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { StudyEnvironmentSurvey } from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'

/** renders a modal that allows archiving a survey from the current study env */
const ArchiveSurveyModal = ({ studyEnvContext, selectedSurveyConfig, onDismiss }: {
  studyEnvContext: StudyEnvContextT, selectedSurveyConfig: StudyEnvironmentSurvey,
  onDismiss: () => void }) => {
  const [isLoading, setIsLoading] = useState(false)

  const portalContext = useContext(PortalContext) as PortalContextT

  const [confirmArchiveSurvey, setConfirmArchiveSurvey] = useState('')
  const archiveString = `archive ${selectedSurveyConfig.survey.name}`
  const canArchive = confirmArchiveSurvey.toLowerCase() === archiveString.toLowerCase()

  const archiveSurvey = async () => {
    setIsLoading(true)

    await Api.removeConfiguredSurvey(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      selectedSurveyConfig.id
    ).catch(() =>
      Store.addNotification(failureNotification('Error removing survey'))
    )
    await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    setIsLoading(false)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Archive Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Are you sure you want to archive the <strong>{selectedSurveyConfig.survey.name}</strong> survey
        from the {studyEnvContext.currentEnv.environmentName} environment? This will not
        delete existing participant responses to this survey.
      </div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
          Confirm by typing &quot;{archiveString}&quot; below.<br/>
          <input type="text" size={50} className="form-control" id="inputSurveyRemoval" value={confirmArchiveSurvey}
            onChange={event => setConfirmArchiveSurvey(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-danger"
          disabled={!canArchive}
          onClick={archiveSurvey}
        >Archive survey from {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}</button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default ArchiveSurveyModal
