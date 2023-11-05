import React, {useEffect, useRef, useState} from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { useNavigate, useParams } from 'react-router-dom'
import Select from 'react-select'
import TestEmailSender from './TestEmailSender'
import Api, {NotificationConfig} from 'api/api'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import {doApiLoad, useLoadingEffect} from "api/api-utils";
import {LoadedPortalContextT} from "portal/PortalProvider";
import LoadingSpinner from "util/LoadingSpinner";
import EmailEditor, { EditorRef, EmailEditorProps } from 'react-email-editor';
import EmailTemplateEditor from "./EmailTemplateEditor";

const configTypeOptions = [{ label: 'Event', value: 'EVENT' }, { label: 'Task reminder', value: 'TASK_REMINDER' },
  { label: 'Ad hoc', value: 'AD_HOC' }]
const deliveryTypeOptions = [{ label: 'Email', value: 'EMAIL' }]
const eventTypeOptions = [{ label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' }, { label: 'Survey Response', value: 'SURVEY_RESPONSE' }]
const taskTypeOptions = [{ label: 'Survey', value: 'SURVEY' }, { label: 'Consent', value: 'CONSENT' }]


/** for viewing and editing a notification config.  saving not yet implemented */
export default function NotificationConfigView({ studyEnvContext, portalContext}:
{studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const { currentEnv, portal, study, currentEnvPath } = studyEnvContext
  const [showSendModal, setShowSendModal] = useState(false)
  const navigate = useNavigate()

  const configId = useParams().configId
  const [config, setConfig] = useState<NotificationConfig>()
  const [workingConfig, setWorkingConfig] = useState<NotificationConfig>()
  const hasTemplate = !!workingConfig?.emailTemplate
  const isTaskReminder = workingConfig?.notificationType === 'TASK_REMINDER'
  const isEventConfig = workingConfig?.notificationType === 'EVENT'

  const {isLoading, setIsLoading} = useLoadingEffect(async () => {
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
    }, {setIsLoading})
  }



  return <div>
    {!isLoading && !!workingConfig && <form className="bg-white p-3 my-2">
      <div >
        <label className="form-label">Notification type
          <Select options={configTypeOptions}
            value={configTypeOptions.find(opt => opt.value === workingConfig.notificationType)}
            onChange={opt =>
                setWorkingConfig({ ...workingConfig, notificationType: opt?.value ?? configTypeOptions[0].value })}
          />
        </label>
      </div>
      { isEventConfig && <div>
        <label className="form-label">Event name
          <Select options={eventTypeOptions}
            value={eventTypeOptions.find(opt => opt.value === workingConfig.eventType)}
            onChange={opt =>
                setWorkingConfig({ ...workingConfig, eventType: opt?.value ?? eventTypeOptions[0].value })}
          />
        </label>
      </div> }
      { isTaskReminder && <div>
        <div>
          <label className="form-label">Task type
            <Select options={taskTypeOptions}
              value={taskTypeOptions.find(opt => opt.value === workingConfig.taskType)}
              onChange={opt => setWorkingConfig({ ...workingConfig, taskType: opt?.value ?? taskTypeOptions[0].value})}
            />
          </label>
        </div>
        <div>
          <label className="form-label">Remind after
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="number" value={workingConfig.afterMinutesIncomplete}
                onChange={e => setWorkingConfig(
                  { ...workingConfig, afterMinutesIncomplete: parseInt(e.target.value) || 0 }
                )}/>
              minutes
            </div>
          </label>
        </div>
        <div>
          <label className="form-label">Repeat reminder after
            <div className="d-flex align-items-center">
              <input className="form-control me-2" type="number" value={workingConfig.reminderIntervalMinutes}
                onChange={e => setWorkingConfig(
                  { ...workingConfig, reminderIntervalMinutes: parseInt(e.target.value) || 0 }
                )}
              />
              minutes</div>
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
                                            updateEmailTemplate={updatedTemplate => setWorkingConfig({
                                              ...workingConfig,
                                              emailTemplate: {
                                                ...updatedTemplate,
                                                id: undefined,
                                                version: config!.emailTemplate.version + 1
                                              }
                                            })}/>}

      <div className="d-flex justify-content-center">
        <button type="button" className="btn btn-primary" onClick={saveConfig}>Save</button>
        <button type="button" className="btn btn-secondary ms-4"
          onClick={() => setShowSendModal(true)}>Send test email</button>
      </div>
      <TestEmailSender portalShortcode={portal.shortcode} environmentName={currentEnv.environmentName}
        show={showSendModal} setShow={setShowSendModal} notificationConfig={workingConfig}/>
    </form> }
    { isLoading && <LoadingSpinner/> }
  </div>
}
