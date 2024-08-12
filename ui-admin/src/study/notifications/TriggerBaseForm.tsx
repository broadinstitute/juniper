import {
  Trigger,
  TriggerActionType,
  TriggerType
} from '@juniper/ui-core'
import { NavLink } from 'react-router-dom'
import Select from 'react-select'
import React from 'react'

const eventTypeOptions = [
  { label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' },
  { label: 'Survey Response', value: 'SURVEY_RESPONSE' },
  { label: 'Kit Sent', value: 'KIT_SENT' },
  { label: 'Kit Returned', value: 'KIT_RECEIVED' }
]
const taskTypeOptions = [
  { label: 'Survey', value: 'SURVEY' },
  { label: 'Outreach', value: 'OUTREACH' },
  { label: 'Consent', value: 'CONSENT' },
  { label: 'Kit request', value: 'KIT_REQUEST' }
]

const actionTypeOptions: {label: string, value: TriggerActionType }[] = [
  { label: 'Participant Notification', value: 'NOTIFICATION' },
  { label: 'Admin Notification', value: 'ADMIN_NOTIFICATION' },
  { label: 'Update task status', value: 'TASK_STATUS_CHANGE' }
]

const configTypeOptions: { label: string, value: TriggerType}[] = [
  { label: 'Event', value: 'EVENT' },
  { label: 'Task reminder', value: 'TASK_REMINDER' },
  { label: 'Ad hoc', value: 'AD_HOC' }
]

/** configures the notification type and event/task type */
export default function TriggerBaseForm({ trigger, setTrigger }:
  {trigger: Trigger, setTrigger: (trigger: Trigger) => void}) {
  return <>
    <div>
      { trigger.id && <div className="float-end">
        <NavLink to='notifications'>View sent emails</NavLink>
      </div> }
      <label className="form-label" htmlFor="triggerType">Trigger</label>
      <Select options={configTypeOptions} inputId="triggerType"
        value={configTypeOptions.find(opt => opt.value === trigger.triggerType)}
        onChange={opt =>
          setTrigger({ ...trigger, triggerType: opt?.value ?? configTypeOptions[0].value })}
      />
    </div>
    <div>
      <label className="form-label mt-3" htmlFor="triggerAction">Action</label>
      <Select options={actionTypeOptions} inputId="triggerAction"
        value={actionTypeOptions.find(opt => opt.value === trigger.actionType)}
        onChange={opt =>
          setTrigger({ ...trigger, actionType: opt?.value ?? actionTypeOptions[0].value })}
      />
    </div>
    { isEventConfig(trigger) && <div>
      <label className="form-label mt-3" htmlFor="eventName">Event name</label>
      <Select options={eventTypeOptions} inputId="eventName"
        value={eventTypeOptions.find(opt => opt.value === trigger.eventType)}
        onChange={opt =>
          setTrigger({ ...trigger, eventType: opt?.value ?? eventTypeOptions[0].value })}
      />
    </div> }
    { isTaskReminder(trigger) && <div>
      <label className="form-label mt-3" htmlFor="taskType">Task type</label>
      <Select options={taskTypeOptions} inputId="taskType"
        value={taskTypeOptions.find(opt => opt.value === trigger.taskType)}
        onChange={opt => setTrigger({
          ...trigger, taskType: opt?.value ??
                    taskTypeOptions[0].value
        })}
      />
    </div> }
  </>
}


/**
 *
 */
export const isTaskReminder = (config?: Trigger) => config?.triggerType === 'TASK_REMINDER'
/**
 *
 */
export const isEventConfig = (config?: Trigger) => config?.triggerType === 'EVENT'
/**
 *
 */
export const isNotification = (config?: Trigger) => config?.actionType === 'NOTIFICATION'
export const isAdminNotification = (config?: Trigger) => config?.actionType === 'ADMIN_NOTIFICATION'
/**
 *
 */
export const isAction = (config?: Trigger) => config?.actionType === 'TASK_STATUS_CHANGE'

