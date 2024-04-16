import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { NavLink, useNavigate, useParams } from 'react-router-dom'
import Select from 'react-select'
import TestEmailSender from './TestEmailSender'
import Api, { Trigger } from 'api/api'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import EmailTemplateEditor from './EmailTemplateEditor'
import { Modal } from 'react-bootstrap'
import useReactSingleSelect from 'util/react-select-utils'
import { PortalEnvironmentLanguage } from '@juniper/ui-core'
import { usePortalLanguage } from 'portal/useDefaultPortalLanguage'

const configTypeOptions = [{ label: 'Event', value: 'EVENT' }, { label: 'Task reminder', value: 'TASK_REMINDER' },
  { label: 'Ad hoc', value: 'AD_HOC' }]
const deliveryTypeOptions = [{ label: 'Email', value: 'EMAIL' }]
const eventTypeOptions = [{ label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' }, { label: 'Survey Response', value: 'SURVEY_RESPONSE' },
  { label: 'Kit Sent', value: 'KIT_SENT' }, { label: 'Kit Returned', value: 'KIT_RECEIVED' }]
const taskTypeOptions = [{ label: 'Survey', value: 'SURVEY' }, { label: 'Consent', value: 'CONSENT' },
  { label: 'Kit request', value: 'KIT_REQUEST' }]


/** for viewing and editing a notification config.  saving not yet implemented */
export default function TriggerView({ studyEnvContext, portalContext, onDelete }:
                                                 {
                                                   studyEnvContext: StudyEnvContextT,
                                                   portalContext: LoadedPortalContextT,
                                                   onDelete: () => void
                                                 }) {
  const { currentEnv, portal, study, currentEnvPath } = studyEnvContext
  const { defaultLanguage, supportedLanguages } = usePortalLanguage()
  const [showSendModal, setShowSendModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [selectedLanguage, setSelectedLanguage] = useState<PortalEnvironmentLanguage | undefined>(defaultLanguage)

  const navigate = useNavigate()
  const {
    onChange: languageOnChange, options: languageOptions,
    selectedOption: selectedLanguageOption, selectInputId: selectLanguageInputId
  } =
      useReactSingleSelect(
        supportedLanguages,
        (language: PortalEnvironmentLanguage) => ({ label: language.languageName, value: language }),
        setSelectedLanguage,
        selectedLanguage
      )

  const configId = useParams().configId
  const [config, setConfig] = useState<Trigger>()
  const [workingConfig, setWorkingConfig] = useState<Trigger>()
  const hasTemplate = !!workingConfig?.emailTemplate

  const { isLoading, setIsLoading } = useLoadingEffect(async () => {
    if (!configId) { return }
    const loadedConfig = await Api.findTrigger(portal.shortcode, study.shortcode, currentEnv.environmentName,
      configId)
    setConfig(loadedConfig)
    setWorkingConfig(loadedConfig)
  }, [configId])

  const saveConfig = async () => {
    if (!workingConfig) { return }
    doApiLoad(async () => {
      const savedConfig = await Api.updateTrigger(portal.shortcode,
        currentEnv.environmentName, study.shortcode, workingConfig.id, workingConfig)
      Store.addNotification(successNotification('Notification saved'))
      await portalContext.reloadPortal(portal.shortcode)
      navigate(`${currentEnvPath}/notificationContent/configs/${savedConfig.id}`)
    }, { setIsLoading })
  }

  const deleteConfig = async () => {
    if (configId) {
      await Api.deleteTrigger(portal.shortcode, study.shortcode, currentEnv.environmentName, configId)
      onDelete()
    }
  }

  return <div className={'w-100'}>
    {!isLoading && !!workingConfig && <form className="bg-white my-2">
      <TriggerBaseForm config={workingConfig} setConfig={setWorkingConfig}/>
      { isTaskReminder(workingConfig) && <div>
        <div>
          <label className="form-label mt-3">Remind after
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="number" value={workingConfig.afterMinutesIncomplete / 60}
                onChange={e => setWorkingConfig(
                  { ...workingConfig, afterMinutesIncomplete: parseInt(e.target.value) * 60 || 0 }
                )}/>
              hours
            </div>
          </label>
        </div>
        <div>
          <label className="form-label">Repeat reminder after
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="number" value={workingConfig.reminderIntervalMinutes / 60}
                onChange={e => setWorkingConfig(
                  { ...workingConfig, reminderIntervalMinutes: parseInt(e.target.value) * 60 || 0 }
                )}
              />
              hours</div>
          </label>
        </div>
        <div>
          <label className="form-label">Max reminders
            <input className="form-control" type="number" value={workingConfig.maxNumReminders}
              onChange={e => setWorkingConfig(
                { ...workingConfig, maxNumReminders: parseInt(e.target.value) || 0 }
              )}/>
          </label>
        </div>
      </div>
      }

      <div>
        <label className="form-label">Delivery
          <Select options={deliveryTypeOptions} isDisabled={true}
            value={deliveryTypeOptions.find(opt => opt.value === workingConfig.deliveryType)}/>
        </label>
      </div>

      { supportedLanguages.length > 1 && <div style={{ width: 200 }}>
        <label className="form-label">Language
          <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
            aria-label={'Select a language'}
            onChange={e => {
              languageOnChange(e)
            }}/>
        </label>
      </div> }

      {hasTemplate &&
          <EmailTemplateEditor emailTemplate={workingConfig.emailTemplate}
            selectedLanguage={selectedLanguage}
            portalShortcode={portal.shortcode}
            updateEmailTemplate={updatedTemplate => setWorkingConfig(currentConfig => {
              // we have to use currentConfig since the template editor might call a stale version of this handler
              // due to the unlayer event listener setup
              return {
                ...currentConfig!,
                emailTemplate: {
                  ...updatedTemplate,
                  id: undefined,
                  publishedVersion: undefined,
                  version: config ? config.emailTemplate.version + 1 : 1
                }
              }
            })}/>}

      <div className="d-flex justify-content-center">
        <button type="button" className="btn btn-primary" onClick={saveConfig}>Save</button>
        <button type="button" className="btn btn-secondary ms-4"
          onClick={() => setShowSendModal(true)}>Send test email</button>
        <button type="button" className="btn btn-danger ms-4" onClick={() => setShowDeleteModal(true)}>Delete</button>
      </div>
      {showSendModal && <TestEmailSender studyEnvParams={{
        portalShortcode: portal.shortcode,
        envName: currentEnv.environmentName, studyShortcode: study.shortcode
      }}
      onDismiss={() => setShowSendModal(false)} trigger={workingConfig}/> }
      {showDeleteModal && (
        <Modal show className="modal" onHide={() => setShowDeleteModal(false)}>
          <Modal.Header closeButton className="danger"><strong>Delete Notification Config</strong></Modal.Header>
          <Modal.Body>
            <p className="fst-italic">
              Are you sure you want to delete this notification configuration? This cannot be undone.
            </p>
          </Modal.Body>
          <Modal.Footer>
            <button type="button" className="btn btn-secondary" onClick={() => setShowDeleteModal(false)}>Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={deleteConfig}>Delete</button>
          </Modal.Footer>
        </Modal>
      )}
    </form> }
    <div>
      Note the preview above does not guarantee how the email will appear in all browsers and clients. To test this,
      use the &apos;Send test email&apos; button to send test emails to a given email address.
    </div>
    {isLoading && <LoadingSpinner/>}
  </div>
}

/** configures the notification type and event/task type */
export const TriggerBaseForm = ({ config, setConfig }:
                     {config: Trigger, setConfig: (config: Trigger) => void}) => {
  return <>
    <div>
      <div className='float-end'>
        <NavLink to='notifications'>View sent emails</NavLink>
      </div>
      <label className="form-label" htmlFor="notificationType">Trigger</label>
      <Select options={configTypeOptions} inputId="notificationType"
        value={configTypeOptions.find(opt => opt.value === config.triggerType)}
        onChange={opt =>
          setConfig({ ...config, triggerType: opt?.value ?? configTypeOptions[0].value })}
      />
    </div>
    { isEventConfig(config) && <div>
      <label className="form-label mt-3" htmlFor="eventName">Event name</label>
      <Select options={eventTypeOptions} inputId="eventName"
        value={eventTypeOptions.find(opt => opt.value === config.eventType)}
        onChange={opt =>
          setConfig({ ...config, eventType: opt?.value ?? eventTypeOptions[0].value })}
      />
    </div> }
    { isTaskReminder(config) && <div>
      <label className="form-label mt-3" htmlFor="taskType">Task type</label>
      <Select options={taskTypeOptions} inputId="taskType"
        value={taskTypeOptions.find(opt => opt.value === config.taskType)}
        onChange={opt => setConfig({ ...config, taskType: opt?.value ?? taskTypeOptions[0].value })}
      />
    </div> }
  </>
}

const isTaskReminder = (config?: Trigger) => config?.triggerType === 'TASK_REMINDER'
const isEventConfig = (config?: Trigger) => config?.triggerType === 'EVENT'
