import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { StudyEnvironmentSurvey } from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../../util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'

/** renders a modal that allows removing a survey from the current study env */
const RemoveSurveyModal = ({
  studyEnvContext, selectedSurveyConfig, show, setShow
}: {
  studyEnvContext: StudyEnvContextT, selectedSurveyConfig: StudyEnvironmentSurvey,
  show: boolean, setShow:  React.Dispatch<React.SetStateAction<boolean>> }) => {
  const [isLoading, setIsLoading] = useState(false)

  const portalContext = useContext(PortalContext) as PortalContextT

  const [confirmRemoveSurvey, setConfirmRemoveSurvey] = useState('')
  const removeString = `remove ${selectedSurveyConfig.survey.name}`
  const canRemove = confirmRemoveSurvey.toLowerCase() === removeString.toLowerCase()

  const removeSurvey = async () => {
    setIsLoading(true)

    await Api.removeConfiguredSurvey(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      selectedSurveyConfig.id
    ).catch(() =>
      Store.addNotification(failureNotification('Error removing survey'))
    )

    setShow(false)
    await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    setIsLoading(false)
  }

  return <Modal show={show}
    onHide={() => {
      setShow(false)
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Remove Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        Are you sure you want to remove the <strong>{selectedSurveyConfig.survey.name}</strong> survey
        from the {studyEnvContext.currentEnv.environmentName} environment? This will not
        delete existing participant responses to this survey.
      </div>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label">
          Confirm by typing &quot;{removeString}&quot; below.<br/>
          <input type="text" size={50} className="form-control" id="inputSurveyRemoval" value={confirmRemoveSurvey}
            onChange={event => setConfirmRemoveSurvey(event.target.value)}/>
        </label>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-danger"
          disabled={!canRemove}
          onClick={removeSurvey}
        >Remove survey from {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}</button>
        <button className="btn btn-secondary" onClick={() => {
          setShow(false)
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default RemoveSurveyModal
