import React, { useEffect, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useNavigate, useParams } from 'react-router-dom'
import Select from 'react-select'
import TestEmailSender from './TestEmailSender'
import Api from 'api/api'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

const configTypeOptions = [{ label: 'Event', value: 'EVENT' }, { label: 'Task reminder', value: 'TASK_REMINDER' },
  { label: 'Ad hoc', value: 'AD_HOC' }]
const deliveryTypeOptions = [{ label: 'Email', value: 'EMAIL' }]
const eventTypeOptions = [{ label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' }, { label: 'Survey Response', value: 'SURVEY_RESPONSE' }]
const taskTypeOptions = [{ label: 'Survey', value: 'SURVEY' }, { label: 'Consent', value: 'CONSENT' }]


/** for viewing and editing a notification config.  saving not yet implemented */
export default function NotificationConfigView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv, portal, study, currentEnvPath } = studyEnvContext
  const [showSendModal, setShowSendModal] = useState(false)
  const navigate = useNavigate()

  const configId = useParams().configId
  const matchedConfig  = currentEnv.notificationConfigs.find(cfig => cfig.id === configId)
  const [config, setConfig] = useState(matchedConfig)
  if (!config) {
    return <div>no config with that id exists</div>
  }
  const hasTemplate = !!config.emailTemplate
  const isTaskReminder = config.notificationType === 'TASK_REMINDER'
  const isEventConfig = config.notificationType === 'EVENT'

  const saveConfig = async () => {
    if (!matchedConfig) {
      return
    }
    try {
      const savedConfig = await Api.updateNotificationConfig(portal.shortcode,
        currentEnv.environmentName, study.shortcode, matchedConfig.id, config)
      Store.addNotification(successNotification('Notification saved'))
      const matchedConfigIndex = currentEnv.notificationConfigs.findIndex(cfig => cfig.id === configId)
      if (!matchedConfigIndex) {
        return
      }
      currentEnv.notificationConfigs = [...currentEnv.notificationConfigs]
      currentEnv.notificationConfigs[matchedConfigIndex] = savedConfig
      navigate(`${currentEnvPath}/notificationContent/configs/${savedConfig.id}`)
    } catch {
      Store.addNotification(failureNotification('Save failed'))
    }
  }

  const loadConfig = async () => {
    // we'll want to load the config from the server in the future to not be dependent on initial bulk study loads
    // for now, we just pull it off the study
    setConfig(matchedConfig)
  }

  useEffect(() => {
    if (configId) {
      loadConfig()
    }
  }, [configId])

  return <div>
    <form className="bg-white p-3 my-2">
      <div >
        <label className="form-label">Notification type
          <Select options={configTypeOptions} isDisabled={true}
            value={configTypeOptions.find(opt => opt.value === config.notificationType)}/>
        </label>
      </div>
      { isEventConfig && <div>
        <label className="form-label">Event name
          <Select options={eventTypeOptions} isDisabled={true}
            value={eventTypeOptions.find(opt => opt.value === config.eventType)}/>
        </label>
      </div> }
      { isTaskReminder && <div>
        <div>
          <label className="form-label">Task type
            <Select options={eventTypeOptions} isDisabled={true}
              value={taskTypeOptions.find(opt => opt.value === config.taskType)}/>
          </label>
        </div>
        <div>
          <label className="form-label">Remind after:
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="text" value={config.afterMinutesIncomplete}
                onChange={e => setConfig(
                  { ...config, afterMinutesIncomplete: parseInt(e.target.value) || 0 }
                )}/>
              minutes
            </div>
          </label>
        </div>
        <div>
          <label className="form-label">Repeat reminder after:
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="text" value={config.reminderIntervalMinutes}
                onChange={e => setConfig(
                  { ...config, reminderIntervalMinutes: parseInt(e.target.value) || 0 }
                )}
              />
              minutes</div>
          </label>
        </div>
        <div>
          <label className="form-label">Max reminders:
            <input className="form-control" type="text" value={config.maxNumReminders}
              onChange={e => setConfig(
                { ...config, maxNumReminders: parseInt(e.target.value) || 0 }
              )}/>
          </label>
        </div>
      </div>
      }
      <div>
        <label className="form-label">Delivery
          <Select options={deliveryTypeOptions}  isDisabled={true}
            value={deliveryTypeOptions.find(opt => opt.value === config.deliveryType)}/>
        </label>
      </div>

      { hasTemplate && <div className="mt-3">
        <h6>Email Template</h6>
        <div>
          <label className="form-label">Subject
            <input className="form-control" type="text" size={100} value={config.emailTemplate.subject}
              readOnly={true}/>
          </label>
        </div>
        <textarea rows={20} cols={100} value={config.emailTemplate.body} readOnly={true}/>
      </div>}

      <div className="d-flex justify-content-center">
        <button type="button" className="btn btn-primary" onClick={saveConfig}>Save</button>
        <button type="button" className="btn btn-secondary ms-4"
          onClick={() => setShowSendModal(true)}>Send test email</button>
      </div>
      <TestEmailSender portalShortcode={portal.shortcode} environmentName={currentEnv.environmentName}
        show={showSendModal} setShow={setShowSendModal} notificationConfig={config}/>
    </form>
  </div>
}
