import {
  ParticipantTaskStatus,
  StudyEnvParams,
  Trigger,
  TriggerActionType,
  TriggerDeliveryType,
  TriggerScope,
  TriggerType
} from '@juniper/ui-core'
import React, {
  useId,
  useState
} from 'react'
import Select from 'react-select'
import useReactSingleSelect from 'util/react-select-utils'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardTitle
} from 'components/InfoCard'
import EmailTemplateEditor from 'study/notifications/EmailTemplateEditor'
import {
  paramsFromContext,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
import InfoPopup from 'components/forms/InfoPopup'
import { NavLink } from 'react-router-dom'
import { Checkbox } from 'components/forms/Checkbox'
import { LazySearchQueryBuilder } from 'search/LazySearchQueryBuilder'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'


export const TriggerDesignerEditor = (
  {
    studyEnvContext,
    trigger,
    updateTrigger,
    baseFieldsOnly = false,
    sendTestEmail
  } : {
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    baseFieldsOnly?: boolean
    sendTestEmail?: () => void
  }) => {
  return <div>
    <TriggerTypeEditor
      studyEnvContext={studyEnvContext}
      baseFieldsOnly={baseFieldsOnly}
      trigger={trigger}
      updateTrigger={updateTrigger}
      studyEnvParams={paramsFromContext(studyEnvContext)}
    />
    <TriggerActionEditor
      baseFieldsOnly={baseFieldsOnly}
      studyEnvContext={studyEnvContext}
      trigger={trigger}
      updateTrigger={updateTrigger}
      sendTestEmail={sendTestEmail}
    />
  </div>
}


const triggerTypeLabels = {
  EVENT: 'Event',
  TASK_REMINDER: 'Task Reminder',
  AD_HOC: 'Ad hoc'
}

const TriggerTypeEditor = (
  {
    baseFieldsOnly,
    studyEnvContext,
    trigger,
    updateTrigger,
    studyEnvParams
  }: {
    baseFieldsOnly: boolean
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    studyEnvParams: StudyEnvParams
  }
) => {
  const updateTriggerType = (val: TriggerType | undefined) => {
    updateTrigger('triggerType', val)
  }

  const {
    onChange: onTriggerTypeChange,
    options: triggerTypeOptions,
    selectedOption: triggerType,
    selectInputId: triggerTypeSelectInputId
  } = useReactSingleSelect(
    ['EVENT', 'TASK_REMINDER', 'AD_HOC'],
    val => {
      return {
        value: val,
        label: triggerTypeLabels[val]
      }
    },
    updateTriggerType,
    trigger.triggerType)

  const isTaskScopable = (trigger.triggerType === 'TASK_REMINDER') ||
    (trigger.triggerType === 'EVENT' && ['SURVEY_RESPONSE', 'KIT_RECEIVED', 'KIT_SENT'].includes(trigger.eventType))

  return <InfoCard>
    <InfoCardHeader>
      <InfoCardTitle title={'Condition'}/>
    </InfoCardHeader>
    <InfoCardBody>
      <div className='w-100'>
        <div className='d-flex align-items-baseline w-100'>
          <label className={'me-1'} htmlFor={triggerTypeSelectInputId}>
            Trigger action on
          </label>
          <Select
            options={triggerTypeOptions}
            value={triggerType}
            onChange={onTriggerTypeChange}
            inputId={triggerTypeSelectInputId}
          />
          {trigger.triggerType === 'EVENT'
            && <EventTriggerEditor trigger={trigger} updateTrigger={updateTrigger}/>}
          {trigger.triggerType === 'TASK_REMINDER'
           && <TaskReminderTypeEditor trigger={trigger} updateTrigger={updateTrigger}/>}

        </div>

        {!baseFieldsOnly && <>
          {isTaskScopable && <TaskTargetStableIdsEditor
            studyEnvParams={studyEnvParams}
            stableIds={trigger.filterTargetStableIds}
            setStableIds={ids => updateTrigger('filterTargetStableIds', ids)}
            isKitType={['KIT_RECEIVED', 'KIT_SENT'].includes(trigger.eventType)}
          />}

          {trigger.triggerType !== 'AD_HOC' &&
              <HorizontalBar/>}

          {trigger.triggerType === 'TASK_REMINDER'
            && <>
              <TaskReminderEditor trigger={trigger} updateTrigger={updateTrigger}/>
            </>}

          {trigger.triggerType !== 'AD_HOC' && <TriggerRuleEditor
            studyEnvContext={studyEnvContext}
            trigger={trigger}
            updateTrigger={updateTrigger}/>}
        </>
        }

      </div>
    </InfoCardBody>
  </InfoCard>
}

const HorizontalBar = () => {
  return <div
    className='w-100 border-top border-1 my-3'
  />
}

const eventTypeOptions = [
  { label: 'Study Enrollment', value: 'STUDY_ENROLLMENT' },
  { label: 'Study Consent', value: 'STUDY_CONSENT' },
  { label: 'Survey Completed', value: 'SURVEY_RESPONSE' },
  { label: 'Kit Sent', value: 'KIT_SENT' },
  { label: 'Kit Returned', value: 'KIT_RECEIVED' },
  { label: 'Failed Login', value: 'FAILED_LOGIN' }
]

const EventTriggerEditor = (
  {
    trigger,
    updateTrigger
  } : {
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }) => {
  return <div className="d-flex ms-2 align-items-baseline">
    <Select
      options={eventTypeOptions}
      inputId="eventName"
      aria-label='Event type'
      value={eventTypeOptions.find(opt => opt.value === trigger.eventType)}
      onChange={opt =>
        updateTrigger('eventType', opt?.value ?? eventTypeOptions[0].value)}
    />
  </div>
}

const taskTypeOptions = [
  { label: 'Surveys', value: 'SURVEY' },
  { label: 'Outreach surveys', value: 'OUTREACH' },
  { label: 'Consent surveys', value: 'CONSENT' },
  { label: 'Kit requests', value: 'KIT_REQUEST' }
]
const TaskReminderTypeEditor = (
  {
    trigger,
    updateTrigger
  }: {
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  return <>
    <span className="mx-2">for</span>
    <Select options={taskTypeOptions} inputId="taskType"
      value={taskTypeOptions.find(opt => opt.value === trigger.taskType)}
      aria-label={'Task type'}
      onChange={opt => updateTrigger(
        'taskType',
        opt?.value ??
                taskTypeOptions[0].value)}/>
  </>
}

const TaskReminderEditor = (
  {
    trigger,
    updateTrigger
  }: {
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }) => {
  return <div>
    <div>
      <label className="form-label">Remind after
        <div className="d-flex align-items-center">
          <input className="form-control me-2" type="number" value={trigger.afterMinutesIncomplete / 60}
            onChange={e => updateTrigger(
              'afterMinutesIncomplete', parseInt(e.target.value) * 60 || 0
            )}/>
          hours
        </div>
      </label>
    </div>
    <div>
      <label className="form-label">Repeat reminder after
        <div className="d-flex align-items-center">
          <input className="form-control me-2" type="number" value={trigger.reminderIntervalMinutes / 60}
            onChange={e => updateTrigger(
              'reminderIntervalMinutes',
              parseInt(e.target.value) * 60 || 0)}/>
          hours</div>
      </label>
    </div>
    <div>
      <label className="form-label">Max reminders
        <input className="form-control" type="number" value={trigger.maxNumReminders}
          onChange={e => updateTrigger(
            'maxNumReminders', parseInt(e.target.value) || 0
          )}/>
      </label>
    </div>
  </div>
}

const TriggerRuleEditor = (
  {
    studyEnvContext,
    trigger,
    updateTrigger
  }: {
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  return <>
    <Checkbox
      checked={trigger.rule !== undefined}
      onChange={checked => updateTrigger('rule', checked ? '' : undefined)}
      label={'Only trigger if enrollee meets certain criteria'}
    />
    {trigger.rule !== undefined && <>
      <LazySearchQueryBuilder
        studyEnvContext={studyEnvContext}
        onSearchExpressionChange={exp => updateTrigger('rule', exp)}
        searchExpression={trigger.rule}/>
    </>}
  </>
}

const actionTypeLabels = {
  NOTIFICATION: 'Send Notification to Participant',
  ADMIN_NOTIFICATION: 'Send Notification to Study Staff',
  TASK_STATUS_CHANGE: 'Change Task Status'
}

const TriggerActionEditor = (
  {
    baseFieldsOnly,
    studyEnvContext,
    trigger,
    updateTrigger,
    sendTestEmail
  }: {
    baseFieldsOnly: boolean
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    sendTestEmail?: () => void
  }
) => {
  const updateActionType = (val: TriggerActionType | undefined) => {
    updateTrigger('actionType', val)
  }

  const {
    onChange: onActionTypeChange,
    options: actionTypeOptions,
    selectedOption: actionType,
    selectInputId: actionTypeSelectInputId
  } = useReactSingleSelect<TriggerActionType>(
    ['NOTIFICATION', 'ADMIN_NOTIFICATION', 'TASK_STATUS_CHANGE'],
    val => {
      return {
        value: val,
        label: actionTypeLabels[val]
      }
    },
    updateActionType,
    trigger.actionType)

  return <InfoCard>
    <InfoCardHeader>
      <InfoCardTitle title={'Action'}/>
    </InfoCardHeader>
    <InfoCardBody>
      <div className='w-100'>
        <div className='d-flex align-items-baseline'>
          <span className={'me-1'}>
            When triggered
          </span>
          <Select
            options={actionTypeOptions}
            value={actionType}
            onChange={onActionTypeChange}
            inputId={actionTypeSelectInputId}
          />
        </div>

        {!baseFieldsOnly && <>
          <div
            className='w-100 border-top border-1 my-3'
          />

          {(trigger.actionType === 'NOTIFICATION' || trigger.actionType === 'ADMIN_NOTIFICATION')
            && <NotificationEditor
              studyEnvContext={studyEnvContext}
              trigger={trigger}
              updateTrigger={updateTrigger}
              sendTestEmail={sendTestEmail}
            />}

          {trigger.actionType === 'TASK_STATUS_CHANGE'
            && <TaskStatusEditor trigger={trigger} updateTrigger={updateTrigger}/>
          }
        </>
        }

      </div>
    </InfoCardBody>
  </InfoCard>
}

const deliveryTypeOptions: { label: string, value: TriggerDeliveryType}[] = [
  { label: 'Email', value: 'EMAIL' }
]


const NotificationEditor = (
  {
    studyEnvContext,
    trigger,
    updateTrigger,
    sendTestEmail
  } : {
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
    sendTestEmail?: () => void
  }) => {
  const hasEmailTemplate = !!trigger?.emailTemplate

  return <>
    <div className="float-end position-relative">
      <NavLink to='notifications'>View sent notifications</NavLink>
    </div>
    {
      trigger.actionType === 'ADMIN_NOTIFICATION'
      && <div className='w-50 mb-2'>
        <TargetEmailEditor
          studyEnvContext={studyEnvContext}
          trigger={trigger}
          updateTrigger={updateTrigger}/>
      </div>
    }
    <label className="form-label">
      Notification Type <InfoPopup content={'Juniper only supports reminders via email.'}/>
      <Select options={deliveryTypeOptions} isDisabled={true}
        value={deliveryTypeOptions.find(opt => opt.value === trigger.deliveryType)}/>
    </label>
    {trigger.actionType === 'NOTIFICATION' && <div className='w-50 mb-2 d-flex flex-row'>
      <label className="form-label">
          Minimum minutes since last notification
        <InfoPopup content={
          'If specified, notifications will be skipped if an email has been ' +
            'recently sent within the specified number of minutes.'
        }/>
        <input
          className="form-control "
          value={trigger.minMinutesSinceLastNotification}
          onChange={e => {
            if (e.target.value === '') {
              updateTrigger('minMinutesSinceLastNotification', undefined)
            } else {
              updateTrigger(
                'minMinutesSinceLastNotification',
                parseInt(e.target.value, 10)
              )
            }
          }}/>
      </label>
    </div>}

    {hasEmailTemplate &&
        <>
          <EmailTemplateEditor
            emailTemplate={trigger.emailTemplate}
            portalShortcode={studyEnvContext.portal.shortcode}
            updateEmailTemplate={updatedTemplate => {
              updateTrigger('emailTemplate', updatedTemplate)
            }
            }
          />
          {sendTestEmail && <div className="d-flex justify-content-center mt-2">
            <button type="button" className="btn btn-secondary ms-4"
              onClick={() => sendTestEmail()}>Send test email
            </button>
          </div>}
        </>}
  </>
}

const TargetEmailEditor = (
  {
    studyEnvContext,
    trigger,
    updateTrigger
  }: {
    studyEnvContext: StudyEnvContextT,
    trigger: Trigger;
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  const [adminUsers, setAdminUsers] = useState<string[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const adminUsers = await Api.fetchAdminUsersByPortal(studyEnvContext.portal.shortcode)
    setAdminUsers(adminUsers.map(user => user.username))
  }, [])


  if (isLoading) {
    return <LoadingSpinner/>
  }

  return <div>
    <label className="form-label" htmlFor="targetEmailEditor">
      Send notification to
    </label>

    <Select
      options={adminUsers.map(username => ({ label: username, value: username }))}
      inputId="targetEmailEditor"
      value={adminUsers
        .filter(username => trigger.targetEmails?.includes(username))
        .map(username => ({ label: username, value: username }))}
      isMulti={true}
      onChange={options => updateTrigger('targetEmails', options.map(opt => opt!.value).join(','))}
    />
  </div>
}

const statusOptions: { label: string, value: ParticipantTaskStatus }[] = [
  { label: 'New', value: 'NEW' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Completed', value: 'COMPLETE' },
  { label: 'Declined', value: 'REJECTED' }
]

const scopeOptions: {label: string, value: TriggerScope}[] = [
  { label: 'Portal', value: 'PORTAL' },
  { label: 'Study', value: 'STUDY' }
]

const TaskStatusEditor = (
  {
    trigger,
    updateTrigger
  } : {
    trigger: Trigger,
    updateTrigger: (string: keyof Trigger, value: unknown) => void;
  }
) => {
  return <div>
    <div>
      <label className="form-label" htmlFor="triggerScope">Action scope</label> <InfoPopup content={
        'Whether the action is confined to the study, or can impact tasks in the portal.'}/>
      <Select options={scopeOptions} inputId="triggerScope"
        value={scopeOptions.find(opt => opt.value === trigger.actionScope)}
        onChange={opt =>
          updateTrigger('actionScope', opt?.value ?? scopeOptions[0].value)}
      />
    </div>
    <div>
      <label className="form-label mt-3" htmlFor="updateToStatus">Updated status</label> <InfoPopup content={
        'The status the task will be updated to when the trigger is activated.'}/>
      <Select options={statusOptions} inputId="updateToStatus"
        value={statusOptions.find(opt => opt.value === trigger.statusToUpdateTo)}
        onChange={opt => updateTrigger('statusToUpdateTo', opt?.value ?? statusOptions[0].value)}
      />
    </div>
    <div>
      <label className="form-label mt-3" htmlFor="updateTaskTargetStableIds">Target stable id </label>
      <InfoPopup content={<span>
            the stable id of the task to update. For survey tasks, this is the survey stable id.
      </span>}/>
    </div>
  </div>
}

const TaskTargetStableIdsEditor = ({ studyEnvParams, stableIds, setStableIds, isKitType }:
  {studyEnvParams: StudyEnvParams, stableIds: string[], setStableIds: (ids: string[]) => void, isKitType: boolean}) => {
  const [options, setOptions] = useState<{ label: string, value: string }[]>([])
  useLoadingEffect(async () => {
    if (isKitType) {
      const kitTypes = await Api.fetchKitTypes(studyEnvParams)
      setOptions(kitTypes.map(kitType => ({ label: kitType.displayName, value: kitType.name })))
    } else {
      const studyEnvSurveys = await Api.findConfiguredSurveys(studyEnvParams.portalShortcode,
        studyEnvParams.studyShortcode, studyEnvParams.envName, true)
      setOptions(studyEnvSurveys.map(ses => ({ label: ses.survey.name, value: ses.survey.stableId })))
    }
  }, [isKitType])

  stableIds = stableIds ?? []
  const inputId = useId()
  return <div className="mt-3">
    <label className="form-label" htmlFor={inputId}>Limit to these {isKitType ? 'kit types' : 'surveys'}
      <span className="fst-italic ms-2">(leave blank if automation applies to all)</span></label>
    <Select options={options} inputId={inputId}
      value={stableIds.map(stableId => options.find(option => option.value === stableId))}
      isMulti={true}
      onChange={options => setStableIds(options.map(opt => opt!.value))}/>
  </div>
}
