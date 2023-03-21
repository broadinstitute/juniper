import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useParams } from 'react-router-dom'
import Select from 'react-select'
import TestEmailSender from './TestEmailSender'
import { cloneDeep } from 'lodash'

const configTypeOptions = [{ label: 'Event', value: 'EVENT' }, { label: 'Task reminder', value: 'TASK_REMINDER' }]
const deliveryTypeOptions = [{ label: 'Email', value: 'EMAIL' }]
const eventTypeOptions = [{ label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' }]
const taskTypeOptions = [{ label: 'Survey', value: 'SURVEY' }, { label: 'Consent', value: 'CONSENT' }]


/** for viewing and editing a notification config.  saving not yet implemented */
export default function NotificationConfigView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv, portal } = studyEnvContext
  const [showSendModal, setShowSendModal] = useState(false)

  const configId = useParams().configId

  const matchedConfig  = currentEnv.notificationConfigs.find(cfig => cfig.id === configId)
  const [config, setConfig] = useState(matchedConfig)
  if (!config) {
    return <div>no config with that id exists</div>
  }
  const hasTemplate = !!config.emailTemplate
  const isTaskReminder = config.notificationType === 'TASK_REMINDER'
  const isEventConfig = config.notificationType === 'EVENT'

  const updateEmailBody = (newBody: string) => {
    const newConfig = cloneDeep(config)
    newConfig.emailTemplate.body = newBody
    setConfig(newConfig)
  }

  return <div className="row justify-content-center">
    <div className="col-md-8 p-2">
      <h5>Configure notification</h5>
      <form className="bg-white p-3 my-2">
        <div >
          <label className="form-label">Notification type
            <Select options={configTypeOptions} isDisabled={true}
              value={configTypeOptions.find(opt => opt.value === config.notificationType)}/>
          </label>
        </div>
        { isEventConfig && <div>
          <label className="form-label">Event name
            <Select options={eventTypeOptions}
              value={eventTypeOptions.find(opt => opt.value === config.eventType)}/>
          </label>
        </div> }
        { isTaskReminder && <div>
          <div>
            <label className="form-label">Task type
              <Select options={eventTypeOptions}
                value={taskTypeOptions.find(opt => opt.value === config.taskType)}/>
            </label>
          </div>
          <div>
            <label className="form-label">Remind after:
              <div className="d-flex"> <input className="form-control" type="text" readOnly={true}
                value={config.afterMinutesIncomplete}/> minutes</div>
            </label>
            <label className="form-label ms-3">Repeat reminder after:
              <div className="d-flex"><input className="form-control" type="text"
                readOnly={true} value={config.reminderIntervalMinutes}/>
                minutes</div>
            </label>
            <label className="form-label ms-3">Max reminders:
              <input className="form-control" type="text" readOnly={true} value={config.maxNumReminders}/>
            </label>
          </div>
        </div>
        }
        <div>
          <label className="form-label">Delivery
            <Select options={deliveryTypeOptions}
              value={deliveryTypeOptions.find(opt => opt.value === config.deliveryType)}/>
          </label>
        </div>

        { hasTemplate && <div className="mt-3">
          <h6>Email Template</h6>
          <div>
            <label className="form-label">Subject
              <input className="form-control" type="text" size={100} value={config.emailTemplate.subject}/>
            </label>
          </div>
          <textarea rows={20} cols={100} value={config.emailTemplate.body}
            onChange={e => updateEmailBody(e.target.value)}/>
        </div>}

        <div className="d-flex justify-content-center">
          <button type="button" className="btn btn-primary" onClick={() => alert('not yet implemented')}>Save</button>
          <button type="button" className="btn btn-secondary ms-4"
            onClick={() => setShowSendModal(true)}>Send test email</button>
        </div>
        <TestEmailSender portalShortcode={portal.shortcode} environmentName={currentEnv.environmentName}
          show={showSendModal} setShow={setShowSendModal} notificationConfig={config}/>
      </form>
    </div>
  </div>
}
