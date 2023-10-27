import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { useNavigate } from 'react-router-dom'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import InfoPopup from 'components/forms/InfoPopup'
import { generateStableId } from 'util/pearlSurveyUtils'
import { ApiErrorResponse, defaultApiErrorHandle, doApiLoad } from 'api/api-utils'
import Api from 'api/api'

/** renders a modal that creates a new survey in a portal and configures it to the current study env */
const CreateSurveyModal = ({ studyEnvContext, onDismiss }:
                               {studyEnvContext: StudyEnvContextT, onDismiss: () => void}) => {
  const [isLoading, setIsLoading] = useState(false)

  const portalContext = useContext(PortalContext) as PortalContextT
  const navigate = useNavigate()
  const { formName, formStableId, clearFields, nameInput, stableIdInput } = useFormCreationNameFields()
  const createSurvey = async () => {
    doApiLoad(async () => {
      const createdSurvey = await Api.createNewSurvey(studyEnvContext.portal.shortcode,
        {
          createdAt: 0, id: '', lastUpdatedAt: 0, version: 1,
          content: '{"pages":[]}', name: formName, stableId: formStableId
        })
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
      } catch (err) {
        defaultApiErrorHandle(err as ApiErrorResponse, 'Error configuring survey: ')
      }

      await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
      navigate(`surveys/${formStableId}`)
      onDismiss()
    }, { setIsLoading })
  }


  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create New Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label" htmlFor="inputFormName">Survey Name</label>
        { nameInput }
        <label className="form-label mt-3" htmlFor="inputFormStableId">Survey Stable ID</label>
        <InfoPopup content={'A stable and unique identifier for the survey. May be shown in exported datasets.'}/>
        { stableIdInput }
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-primary"
          disabled={!formName || !formStableId}
          onClick={createSurvey}
        >Create</button>
        <button className="btn btn-secondary" onClick={() => {
          onDismiss()
          clearFields()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateSurveyModal

/** hook for a form that sets a name and stableId */
export const useFormCreationNameFields = () => {
  const [formName, setFormName] = useState('')
  const [formStableId, setFormStableId] = useState('')
  const [enableAutofillStableId, setEnableAutofillStableId] = useState(true)

  const clearFields = () => {
    setFormName('')
    setFormStableId('')
    setEnableAutofillStableId(true)
  }

  const nameInput = <input type="text" size={50} className="form-control"
    id="inputFormName" value={formName}
    onChange={event => {
      setFormName(event.target.value)
      if (enableAutofillStableId) {
        setFormStableId(generateStableId(event.target.value))
      }
    }}/>

  const stableIdInput = <input type="text" size={50} className="form-control"
    id="inputFormStableId" value={formStableId}
    onChange={event => {
      setFormStableId(event.target.value)
      //Once the user has modified the stable ID on their own,
      // disable autofill in order to prevent overwriting
      setEnableAutofillStableId(false)
    }
    }/>

  return { formName, formStableId, clearFields, nameInput, stableIdInput }
}
