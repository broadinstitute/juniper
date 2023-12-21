import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import { useNavigate } from 'react-router-dom'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import Modal from 'react-bootstrap/Modal'
import InfoPopup from 'components/forms/InfoPopup'
import LoadingSpinner from 'util/LoadingSpinner'
import { useFormCreationNameFields } from './useFormCreationNameFields'
import { ApiErrorResponse, defaultApiErrorHandle, doApiLoad } from 'api/api-utils'

/** dialog for adding a new PreEnrollment survey */
export default function CreatePreEnrollSurveyModal({ studyEnvContext, onDismiss }:
{studyEnvContext: StudyEnvContextT, onDismiss: () => void}) {
  const [isLoading, setIsLoading] = useState(false)

  const portalContext = useContext(PortalContext) as PortalContextT
  const navigate = useNavigate()
  const { formName, formStableId, clearFields, nameInput, stableIdInput } = useFormCreationNameFields()
  const createSurvey =async () => {
    await doApiLoad(async () => {
      const createdSurvey = await Api.createNewSurvey(studyEnvContext.portal.shortcode,
        {
          createdAt: 0, id: '', lastUpdatedAt: 0, version: 1, surveyType: 'RESEARCH',
          content: '{"pages":[]}', name: formName, stableId: formStableId
        })
      Store.addNotification(successNotification('Survey created'))
      try {
        await Api.updateStudyEnvironment(studyEnvContext.portal.shortcode,
          studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName,
          {
            ...studyEnvContext.currentEnv,
            preEnrollSurveyId: createdSurvey.id
          }
        )
      } catch (err) {
        defaultApiErrorHandle(err as ApiErrorResponse, 'Error configuring survey: ')
      }

      await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
      navigate(`preEnroll/${formStableId}`)
    }, { setIsLoading })
    onDismiss()
  }


  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create PreEnrollment Survey</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label" htmlFor="inputFormName">Survey Name</label>
        { nameInput }
        <label className="form-label mt-3" htmlFor="inputFormStableId">Survey Stable ID</label>
        <InfoPopup content={'A stable and unique identifier for the form. May be shown in exported datasets.'}/>
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
