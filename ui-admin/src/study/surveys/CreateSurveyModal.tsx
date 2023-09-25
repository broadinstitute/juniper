import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, { VersionedForm } from 'api/api'
import { useNavigate } from 'react-router-dom'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import InfoPopup from 'components/forms/InfoPopup'
import { generateStableId } from 'util/pearlSurveyUtils'

/** renders a modal that creates a new survey in a portal and configures it to the current study env */
const CreateSurveyModal = ({ studyEnvContext, isReadOnlyEnv, show, setShow }: {studyEnvContext: StudyEnvContextT,
  isReadOnlyEnv: boolean, show: boolean, setShow:  React.Dispatch<React.SetStateAction<boolean>> }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [surveyName, setSurveyName] = useState('')
  const [surveyStableId, setSurveyStableId] = useState('')
  const [enableAutofillStableId, setEnableAutofillStableId] = useState(true)

  const portalContext = useContext(PortalContext) as PortalContextT
  const navigate = useNavigate()

  const createSurvey = async () => {
    setIsLoading(true)
    const createdSurvey = await Api.createNewSurvey(studyEnvContext.portal.shortcode,
      {
        createdAt: 0, id: '', lastUpdatedAt: 0, version: 1,
        content: '{"pages":[]}', name: surveyName, stableId: surveyStableId
      }).catch(e =>
      Store.addNotification(failureNotification(`Error creating survey: ${e.message}`))
    ) as VersionedForm

    try {
      await Api.createConfiguredSurvey(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        {
          allowAdminEdit: true,
          allowParticipantReedit: true,
          allowParticipantStart: true,
          id: '',
          required: false,
          prepopulate: false,
          recurrenceIntervalDays: 0,
          recur: false,
          studyEnvironmentId: studyEnvContext.currentEnv.id,
          survey: createdSurvey,
          surveyId: createdSurvey.id,
          surveyOrder: studyEnvContext.currentEnv.configuredSurveys.length
        }
      )

      await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
      navigate(`surveys/${surveyStableId}?readOnly=${isReadOnlyEnv}`)
    } catch (err) {
      Store.addNotification(
        failureNotification(`Error configuring survey: ${err}`))
    }

    setShow(false)
    setIsLoading(false)
  }
  const clearFields = () => {
    setSurveyName('')
    setSurveyStableId('')
    setEnableAutofillStableId(true)
  }

  return <Modal show={show}
    onHide={() => {
      setShow(false)
      clearFields()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Create New Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label" htmlFor="inputSurveyName">Survey Name</label>
        <input type="text" size={50} className="form-control" id="inputSurveyName" value={surveyName}
          onChange={event => {
            setSurveyName(event.target.value)
            if (enableAutofillStableId) {
              setSurveyStableId(generateStableId(event.target.value))
            }
          }}/>
        <label className="form-label mt-3" htmlFor="inputSurveyStableId">Survey Stable ID</label>
        <InfoPopup content={'A stable and unique identifier for the survey. May be shown in exported datasets.'}/>
        <input type="text" size={50} className="form-control" id="inputSurveyStableId" value={surveyStableId}
          onChange={event => {
            setSurveyStableId(event.target.value)
            //Once the user has modified the stable ID on their own, disable autofill in order to prevent overwriting
            setEnableAutofillStableId(false)
          }
          }/>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-primary"
          disabled={!surveyName || !surveyStableId}
          onClick={createSurvey}
        >Create</button>
        <button className="btn btn-secondary" onClick={() => {
          setShow(false)
          clearFields()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateSurveyModal
