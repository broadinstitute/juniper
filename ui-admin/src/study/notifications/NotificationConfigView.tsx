import React from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useParams } from 'react-router-dom'
import Select from 'react-select'
import Api from '../../api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from '../../util/notifications'

const configTypeOptions = [{ label: 'Event', value: 'EVENT' }, { label: 'Task', value: 'TASK' }]
const deliveryTypeOptions = [{ label: 'Email', value: 'EMAIL' }]
const eventTypeOptions = [{ label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' }]

const exampleRuleData = {
  profile: {
    givenName: 'Tester',
    familyName: 'McTester',
    contactEmail: 'test@test.com'
  },
  enrollee: {
    shortcode: 'TESTER'
  }
}

/** for viewing and editing a notification config.  saving not yet implemented */
export default function NotificationConfigView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv, portal } = studyEnvContext
  const configId = useParams().configId

  const config  = currentEnv.notificationConfigs.find(cfig => cfig.id === configId)
  if (!config) {
    return <div>no config with that id exists</div>
  }
  const hasTemplate = !!config.emailTemplate
  const labelStyle = { minWidth: '10em', maxWidth: '10em' }

  /** sends a test email with the given (saved) notification.  does not currently reflect unsaved changes */
  function sendTestEmail() {
    if (!configId) {
      return
    }
    Api.testNotification(portal.shortcode, currentEnv.environmentName, configId, exampleRuleData).then(() =>
      Store.addNotification(successNotification(
        'Sent test email'
      ))).catch(() =>
      Store.addNotification(failureNotification(
        'error sending test email '
      ))
    )
  }

  return <div className="row justify-content-center">
    <div className="col-md-10">
      <ul className="mt-3">
        <li className="list-group-item d-flex">
          <label style={labelStyle}>Notification type</label>
          <div>
            <Select options={configTypeOptions}
              value={configTypeOptions.find(opt => opt.value === config.notificationType)}/>
          </div>
        </li>
        <li className="list-group-item d-flex">
          <label style={labelStyle}>Event name</label>
          <div>
            <Select options={eventTypeOptions}
              value={eventTypeOptions.find(opt => opt.value === config.eventType)}/>
          </div>
        </li>
        <li className="list-group-item d-flex">
          <label style={labelStyle}>Delivery</label>
          <div>
            <Select options={deliveryTypeOptions}
              value={deliveryTypeOptions.find(opt => opt.value === config.deliveryType)}/>
          </div>
        </li>

        { hasTemplate && <li className="list-group-item d-flex mt-3">
          <label style={labelStyle}>Email Template</label>
          <div>
            <div className="mb-3">
              <label className="me-3">Subject</label>
              <input type="text" size={90} value={config.emailTemplate.subject}/>
            </div>
            <textarea rows={20} cols={100} value={config.emailTemplate.body}/>
          </div>
        </li>}
      </ul>
      <div className="d-flex justify-content-center">
        <button className="btn btn-primary" onClick={() => alert('not yet implemented')}>Save</button>
        <button className="btn btn-secondary ms-4" onClick={sendTestEmail}>Send test email</button>
      </div>

    </div>
  </div>
}
