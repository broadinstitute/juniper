import React, { useContext, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import { useNavigate } from 'react-router-dom'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import InfoPopup from 'components/forms/InfoPopup'
import { ApiErrorResponse, defaultApiErrorHandle, doApiLoad } from 'api/api-utils'
import Api, { Survey } from 'api/api'
import { useFormCreationNameFields } from './useFormCreationNameFields'
import { defaultSurvey, SurveyType } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IconProp } from '@fortawesome/fontawesome-svg-core'
import { faLightbulb, faUsersViewfinder } from '@fortawesome/free-solid-svg-icons'
import { FormOptions } from './FormOptionsModal'
import { useUser } from '../../user/UserProvider'

const QUESTIONNAIRE_TEMPLATE = '{"pages":[{"elements":[]}]}'
const randomSuffix = Math.random().toString(36).substring(2, 15)
const HTML_TEMPLATE = `{"pages":[{"elements":[{"type":"html","name":"outreach_content_${randomSuffix}"}]}]}`

/** renders a modal that creates a new survey in a portal and configures it to the current study env */
const CreateSurveyModal = ({ studyEnvContext, onDismiss, type }:
                               {studyEnvContext: StudyEnvContextT, onDismiss: () => void, type: SurveyType}) => {
  const [isLoading, setIsLoading] = useState(false)
  const { user } = useUser()
  const portalContext = useContext(PortalContext) as PortalContextT
  const navigate = useNavigate()
  // Screeners and research surveys default to an empty form, but marketing
  // outreach defaults to a template with an HTML question. Users can edit that
  // HTML from the survey editor. Alternatively, we could allow them to design the
  // content within this modal and insert the content into the survey on their behalf.

  const [isOutreachScreener, setIsOutreachScreener] = useState(false)

  const [form, setForm] = useState<Survey>({
    ...defaultSurvey,
    autoUpdateTaskAssignments: type === 'OUTREACH',
    assignToExistingEnrollees: type === 'OUTREACH' || type === 'ADMIN',
    allowParticipantReedit: type !== 'CONSENT' && type !== 'ADMIN',
    allowParticipantStart: type !== 'ADMIN',
    allowAdminEdit: type !== 'CONSENT',
    required: type === 'CONSENT',
    stableId: '',
    name: '',
    surveyType: type,
    version: 1,
    content: type === 'OUTREACH' ? HTML_TEMPLATE : QUESTIONNAIRE_TEMPLATE,
    id: '',
    createdAt: new Date().getDate(),
    lastUpdatedAt: new Date().getDate(),
    eligibilityRule: ''
  })

  const { clearFields, NameInput, StableIdInput } = useFormCreationNameFields(form, setForm)

  const createSurvey = async () => {
    doApiLoad(async () => {
      const createdSurvey = await Api.createNewSurvey(studyEnvContext.portal.shortcode,
        form)
      try {
        await Api.createConfiguredSurvey(studyEnvContext.portal.shortcode,
          studyEnvContext.study.shortcode,
          studyEnvContext.currentEnv.environmentName,
          {
            id: '',
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
      navigate(`surveys/${form.stableId}`)
      onDismiss()
    }, { setIsLoading })
  }

  return <Modal show={true} size={'xl'} onHide={onDismiss} className={type === 'OUTREACH' ? 'modal-lg' : 'modal'}>
    <Modal.Header closeButton>
      <Modal.Title>Create new {type.toLowerCase()} form</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label className="form-label" htmlFor="inputFormName">Name</label>
        {NameInput}
        <label className="form-label mt-3" htmlFor="inputFormStableId">Stable ID</label>
        <InfoPopup content={'A stable and unique identifier for the survey. May be shown in exported datasets.'}/>
        {StableIdInput}
        <FormOptions
          studyEnvContext={studyEnvContext}
          initialWorkingForm={form}
          updateWorkingForm={(updates => {
            setForm({ ...form, ...updates })
          })}
        />
        {type === 'OUTREACH' && <>
          <label className="form-label mt-3">Outreach Type</label>
          <div className="row">
            <CardButton
              icon={faLightbulb}
              title="Marketing"
              description="Marketing opportunities allow you to display messages in the participant dashboard."
              onSelect={() => {
                setIsOutreachScreener(false)
                setForm({ ...form, content: HTML_TEMPLATE })
              }}
              isSelected={!isOutreachScreener}
            />
            <CardButton
              icon={faUsersViewfinder}
              title="Screener"
              description="Screener opportunities allow you to send a questionnaire to your participants
              so you can follow up with qualified participants."
              onSelect={() => {
                setIsOutreachScreener(true)
                setForm({ ...form, content: QUESTIONNAIRE_TEMPLATE })
              }}
              isSelected={isOutreachScreener}
            />
          </div>
          <div className="form-group mt-3">
            <label className="form-label" htmlFor="outreachBlurb">Blurb</label>
            <InfoPopup content={'A brief description of your outreach. ' +
                'This will be displayed in the participant dashboard.'}/>
            <textarea className="form-control" id="outreachBlurb" rows={5} value={form.blurb}
              onChange={event =>
                setForm({ ...form, blurb: event.target.value })}/>
          </div>
        </>}
        { user?.superuser && <div className="p-3">
          Import JSON <InfoPopup content="Paste a full JSON Survey model (such as produced by ActivityImporter)."/>
          <input type="text" className="form-control" onChange={e => {
            const importForm: Survey & { jsonContent: string}  = JSON.parse(e.target.value)
            setForm({
              ...defaultSurvey,
              surveyType: type,
              id: '',
              createdAt: Date.now(),
              lastUpdatedAt: Date.now(),
              stableId: importForm.stableId,
              version: importForm.version ?? 1,
              name: importForm.name,
              content: JSON.stringify(importForm.jsonContent)
            })
          }}/>
        </div>}
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button
          className="btn btn-primary"
          disabled={!form.name || !form.stableId}
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

/**
 * Returns a selectable card that acts as a button
 */
export const CardButton = ({ icon, title, description, isSelected, onSelect }: {
  icon: IconProp, title: string, description: string, isSelected: boolean, onSelect: () => void
}) => {
  return (
    <div className="card col mx-3"  style={{ backgroundColor: isSelected ? '#e9ecef' : 'white' }}
      role="button" onClick={onSelect}>
      <div className="card-body">
        <h5 className="card-title"><FontAwesomeIcon icon={icon} /> {title}</h5>
        <p className="card-text text-muted">
          <span>{description}</span>
        </p>
      </div>
    </div>
  )
}

export default CreateSurveyModal
