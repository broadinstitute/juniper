import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { ConsentForm } from '@juniper/ui-core'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import { useNavigate } from 'react-router-dom'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import Modal from 'react-bootstrap/Modal'
import InfoPopup from 'components/forms/InfoPopup'
import LoadingSpinner from 'util/LoadingSpinner'
import { doApiLoad } from 'api/api-utils'
import { useFormCreationNameFields } from 'study/surveys/useFormCreationNameFields'

/** creates a new consent form for a study */
export default function CreateConsentModal({ studyEnvContext, onDismiss }: {
    studyEnvContext: StudyEnvContextT
    onDismiss: () => void }) {
  const [isLoading, setIsLoading] = useState(false)
  const [form, setForm] = useState<ConsentForm>({
    createdAt: 0,
    id: '',
    lastUpdatedAt: 0,
    version: 1,
    content: '{"pages":[]}',
    name: '',
    stableId: ''
  })
  const { clearFields, NameInput, StableIdInput } = useFormCreationNameFields(form, setForm)

  const portalContext = useContext(PortalContext) as PortalContextT
  const navigate = useNavigate()

  const attachConsentToEnv = async (createdConsent: ConsentForm) => {
    await Api.createConfiguredConsent(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      {
        allowAdminEdit: true,
        allowParticipantReedit: true,
        allowParticipantStart: true,
        id: '',
        studyEnvironmentId: studyEnvContext.currentEnv.id,
        consentForm: createdConsent,
        consentFormId: createdConsent.id,
        consentOrder: studyEnvContext.currentEnv.configuredConsents.length,
        prepopulate: false
      }
    )
    Store.addNotification(
      successNotification(`Consent form added to ${studyEnvContext.currentEnv.environmentName}`))
  }

  const createConsent = async () => {
    await doApiLoad(async () => {
      const createdConsent = await Api.createNewConsentForm(studyEnvContext.portal.shortcode, form)

      Store.addNotification(successNotification('Consent form created'))

      await attachConsentToEnv(createdConsent)

      await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
      navigate(`consentForms/${form.stableId}`)
      setIsLoading(false)
      onDismiss()
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create New Consent Form</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label" htmlFor="inputFormName">Consent Name</label>
        { NameInput}
        <label className="form-label mt-3" htmlFor="inputFormStableId">Consent Stable ID</label>
        <InfoPopup content={`A stable and unique identifier for the consent. 
                                      May be shown in exported datasets.`}/>
        { StableIdInput }
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-primary"
          disabled={!form.name || !form.stableId}
          onClick={createConsent}
        >Create</button>
        <button className="btn btn-secondary" onClick={() => {
          onDismiss()
          clearFields()
        }}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
