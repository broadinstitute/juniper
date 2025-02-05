import React, {
  useEffect,
  useState
} from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import Modal from 'react-bootstrap/Modal'
import LoadingSpinner from 'util/LoadingSpinner'
import Api, {
  EnrolleeSearchExpressionResult,
  Trigger
} from 'api/api'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import { Store } from 'react-notifications-component'
import Select from 'react-select'
import {
  ParticipantTaskStatus,
  ParticipantTaskStatusOptions
} from '@juniper/ui-core'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import { isNil } from 'lodash'

export type Recipient = {
  type: 'shortcodes',
  enrolleeShortcodes: string[]
} | {
  type: 'task',
  enrolleesAssignedTaskStableId: string
}
/** modal for letting users send custom emails to seleted participants */
export default function AdHocEmailModal({
  onDismiss,
  studyEnvContext,
  recipient
}: {
  enrolleeShortcodes?: string[], // if specified, send emails to these participants
  studyEnvContext: StudyEnvContextT,
  onDismiss: () => void,
  recipient: Recipient
}) {
  const [isLoading, setIsLoading] = useState(true)
  const [configs, setConfigs] = useState<Trigger[]>([])
  const [selectedConfig, setSelectedConfig] = useState<Trigger | null>(null)
  const [adHocMessage, setAdHocMessage] = useState('')
  const [adHocSubject, setAdHocSubject] = useState('')
  const [searchResults, setSearchResults] = useState<EnrolleeSearchExpressionResult[]>([])
  const [taskStatuses, setTaskStatuses] = useState<ParticipantTaskStatus[]>([
    'NEW', 'IN_PROGRESS', 'COMPLETE', 'VIEWED'
  ])

  useEffect(() => {
    Api.findTriggersForStudyEnv(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
      setConfigs(result)
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification('Could not load notification configs'))
    })
  }, [])


  const sendEmail = async () => {
    if (recipient.type === 'shortcodes') {
      sendEmailToShortcodes()
    } else {
      sendEmailToEnrolleesAssignedTask()
    }
  }
  const sendEmailToShortcodes = async () => {
    if (!selectedConfig) {
      return
    }
    if (recipient.type !== 'shortcodes') {
      return
    }
    try {
      await Api.sendAdHocNotification({
        portalShortcode: studyEnvContext.portal.shortcode,
        studyShortcode: studyEnvContext.study.shortcode,
        envName: studyEnvContext.currentEnv.environmentName,
        enrolleeShortcodes: recipient.enrolleeShortcodes,
        customMessages: { adHocMessage, adHocSubject },
        triggerId: selectedConfig.id
      })
      Store.addNotification(successNotification('email processed'))
    } catch (e) {
      Store.addNotification(failureNotification('email processing failed'))
    }
    onDismiss()
  }

  const buildSearchExp = () => {
    if (recipient.type !== 'task') {
      return '1 = 2' // just in case, match nothing
    }
    return concatSearchExpressions(taskStatuses.map(
      status => `{task.${recipient.enrolleesAssignedTaskStableId}.status}='${status}'`), 'or')
  }

  const taskSearchExp = buildSearchExp()

  useEffect(() => {
    if (recipient.type !== 'task') {
      return
    }

    Api.executeSearchExpression(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, taskSearchExp).then(setSearchResults)
  }, [taskSearchExp])

  const sendEmailToEnrolleesAssignedTask = async () => {
    if (!selectedConfig) {
      return
    }
    if (recipient.type !== 'task') {
      return
    }
    try {
      await Api.sendAdHocNotificationByEnrolleeSearchExp({
        portalShortcode: studyEnvContext.portal.shortcode,
        studyShortcode: studyEnvContext.study.shortcode,
        envName: studyEnvContext.currentEnv.environmentName,
        searchExpression: taskSearchExp,
        customMessages: { adHocMessage, adHocSubject },
        triggerId: selectedConfig.id
      })
      Store.addNotification(successNotification('email processed'))
    } catch (e) {
      Store.addNotification(failureNotification('email processing failed'))
    }
    onDismiss()
  }

  const changeTaskStatus = (statuses: ParticipantTaskStatus[]) => {
    if (statuses.length === 0) {
      setTaskStatuses(['NEW'])
    }
    setTaskStatuses(statuses)
  }

  return <Modal onHide={onDismiss} show={true} className="modal-lg">
    <Modal.Header closeButton>
      <Modal.Title>Send Email</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()} className="py-3">
        {recipient.type === 'task' && <div>
          <label>
                Send to enrollees with task status:
            <Select
              isMulti={true}
              options={ParticipantTaskStatusOptions}
              value={taskStatuses
                .map(status =>
                  ParticipantTaskStatusOptions
                    .find(opt => opt.value === status))}
              onChange={newVals => changeTaskStatus(
                newVals
                  ?.filter(newVal => !isNil(newVal))
                  .map(newVal => newVal!.value))}/>
          </label>
        </div>}

        <label>Email template:
          <Select options={configs} value={selectedConfig} onChange={opt => setSelectedConfig(opt)}
            getOptionLabel={config => config.emailTemplate.name}
            getOptionValue={config => config.id}
            styles={{ control: baseStyles => ({ ...baseStyles, width: '400px' }) }}/>
        </label>
        {selectedConfig?.triggerType === 'AD_HOC' &&
            <div className="py-3">
              <label>Subject:
                <input size={80} value={adHocSubject} onChange={e => setAdHocSubject(e.target.value)}/>
              </label>
              <label>Message:
                <textarea rows={6} cols={80} value={adHocMessage} onChange={e => setAdHocMessage(e.target.value)}/>
              </label>
            </div>
        }
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={sendEmail}>
          Send to {
            recipient.type === 'shortcodes'
              ? recipient.enrolleeShortcodes.length
              : searchResults.length
          } participants
        </button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

