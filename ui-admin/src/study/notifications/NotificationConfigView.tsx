import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useNavigate, useParams } from 'react-router-dom'
import Select from 'react-select'
import TestEmailSender from './TestEmailSender'
import Api, { NotificationConfig } from 'api/api'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import { LoadedPortalContextT } from 'portal/PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import EmailTemplateEditor from './EmailTemplateEditor'

const configTypeOptions = [{ label: 'Event', value: 'EVENT' }, { label: 'Task reminder', value: 'TASK_REMINDER' },
  { label: 'Ad hoc', value: 'AD_HOC' }]
const deliveryTypeOptions = [{ label: 'Email', value: 'EMAIL' }]
const eventTypeOptions = [{ label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' }, { label: 'Survey Response', value: 'SURVEY_RESPONSE' }]
const taskTypeOptions = [{ label: 'Survey', value: 'SURVEY' }, { label: 'Consent', value: 'CONSENT' }]


/** for viewing and editing a notification config.  saving not yet implemented */
export default function NotificationConfigView({ studyEnvContext, portalContext }:
{studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const { currentEnv, portal, study, currentEnvPath } = studyEnvContext
  const [showSendModal, setShowSendModal] = useState(false)
  const navigate = useNavigate()

  const configId = useParams().configId
  const [config, setConfig] = useState<NotificationConfig>()
  const [workingConfig, setWorkingConfig] = useState<NotificationConfig>()
  const hasTemplate = !!workingConfig?.emailTemplate

  const { isLoading, setIsLoading } = useLoadingEffect(async () => {
    if (!configId) { return }
    const loadedConfig = await Api.findNotificationConfig(portal.shortcode, study.shortcode, currentEnv.environmentName,
      configId)
    setConfig(loadedConfig)
    setWorkingConfig(loadedConfig)
  }, [configId])

  const saveConfig = async () => {
    if (!workingConfig) { return }
    doApiLoad(async () => {
      const savedConfig = await Api.updateNotificationConfig(portal.shortcode,
        currentEnv.environmentName, study.shortcode, workingConfig.id, workingConfig)
      Store.addNotification(successNotification('Notification saved'))
      await portalContext.reloadPortal(portal.shortcode)
      navigate(`${currentEnvPath}/notificationContent/configs/${savedConfig.id}`)
    }, { setIsLoading })
  }

  return <div>
    {!isLoading && !!workingConfig && <form className="bg-white p-3 my-2">
      <NotificationConfigBaseForm config={workingConfig} setConfig={setWorkingConfig}/>
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
          <Select options={deliveryTypeOptions}  isDisabled={true}
            value={deliveryTypeOptions.find(opt => opt.value === workingConfig.deliveryType)}/>
        </label>
      </div>

      { hasTemplate && <EmailTemplateEditor emailTemplate={workingConfig.emailTemplate}
        portalShortcode={portal.shortcode}
        updateEmailTemplate={updatedTemplate => setWorkingConfig(currentConfig => {
          // we have to use currentConfig since the template editor might call a stale version of this handler
          // due to the unlayer event listener setup
          return {...currentConfig!,
          emailTemplate: {
            ...updatedTemplate,
            id: undefined,
            version: config ? config.emailTemplate.version + 1 : 1
          }
        }})}/>}

      <div className="d-flex justify-content-center">
        <button type="button" className="btn btn-primary" onClick={saveConfig}>Save</button>
        <button type="button" className="btn btn-secondary ms-4"
          onClick={() => setShowSendModal(true)}>Send test email</button>
      </div>
      {showSendModal && <TestEmailSender portalShortcode={portal.shortcode} environmentName={currentEnv.environmentName}
        onDismiss={() => setShowSendModal(false)} notificationConfig={workingConfig}/> }
    </form> }
    <div>
      Note the preview above does not guarantee how the email will appear in all browsers and clients.  To test this,
        use the &apos;Send test email&apos; button to send test emails to a given email address.
    </div>
    { isLoading && <LoadingSpinner/> }
  </div>
}

/** configures the notification type and event/task type */
export const NotificationConfigBaseForm = ({ config, setConfig }:
                     {config: NotificationConfig, setConfig: (config: NotificationConfig) => void}) => {
  return <>
    <div>
      <label className="form-label" htmlFor="notificationType">Notification type</label>
      <Select options={configTypeOptions} inputId="notificationType"
        value={configTypeOptions.find(opt => opt.value === config.notificationType)}
        onChange={opt =>
          setConfig({ ...config, notificationType: opt?.value ?? configTypeOptions[0].value })}
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

const isTaskReminder = (config?: NotificationConfig) => config?.notificationType === 'TASK_REMINDER'
const isEventConfig = (config?: NotificationConfig) => config?.notificationType === 'EVENT'
