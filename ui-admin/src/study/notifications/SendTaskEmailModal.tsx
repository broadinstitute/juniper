import { ParticipantTaskStatus } from '@juniper/ui-core'
import React, {
  useEffect,
  useState
} from 'react'
import Api, {
  EnrolleeSearchExpressionResult,
  Trigger
} from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import Modal from 'react-bootstrap/Modal'
import Select from 'react-select'
import LoadingSpinner from 'util/LoadingSpinner'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import { isNil } from 'lodash'

export const SendTaskEmailModal = ({ taskStableId, onClose, studyEnvContext }: {
  taskStableId: string,
  onClose: () => void,
  studyEnvContext: StudyEnvContextT
}) => {
  const [isLoading, setIsLoading] = useState(true)
  const [configs, setConfigs] = useState<Trigger[]>([])
  const [selectedConfig, setSelectedConfig] = useState<Trigger | null>(null)
  const [adHocMessage, setAdHocMessage] = useState('')
  const [adHocSubject, setAdHocSubject] = useState('')
  const [taskStatuses, setTaskStatuses] = useState<ParticipantTaskStatus[]>([
    'NEW', 'IN_PROGRESS', 'COMPLETE', 'VIEWED'
  ])
  const changeTaskStatus = (statuses: ParticipantTaskStatus[]) => {
    if (statuses.length === 0) {
      setTaskStatuses(['NEW'])
    }
    setTaskStatuses(statuses)
  }


  const [searchResults, setSearchResults] = useState<EnrolleeSearchExpressionResult[]>([])
  useEffect(() => {
    Api.findTriggersForStudyEnv(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
      setConfigs(result)
      setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification('Could not load notification configs'))
    })
  }, [])

  const buildSearchExp = () => {
    return concatSearchExpressions(taskStatuses.map(status => `{task.${taskStableId}.status}='${status}'`), 'or')
  }

  const searchExp = buildSearchExp()

  useEffect(() => {
    Api.executeSearchExpression(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, searchExp).then(setSearchResults)
  }, [searchExp])

  const sendEmail = async () => {
    if (!selectedConfig) {
      return
    }
    try {
      await Api.sendAdHocNotificationByEnrolleeSearchExp({
        portalShortcode: studyEnvContext.portal.shortcode,
        studyShortcode: studyEnvContext.study.shortcode,
        envName: studyEnvContext.currentEnv.environmentName,
        searchExpression: searchExp,
        customMessages: { adHocMessage, adHocSubject },
        triggerId: selectedConfig.id
      })
      Store.addNotification(successNotification('email processed'))
    } catch (e) {
      Store.addNotification(failureNotification('email processing failed'))
    }
    onClose()
  }

  const taskStatusOptions: { value: ParticipantTaskStatus, label: string}[] = [
    { value: 'NEW', label: 'New' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'COMPLETE', label: 'Complete' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'VIEWED', label: 'Viewed' },
    { value: 'REMOVED', label: 'Removed' }
  ]


  return <Modal onHide={onClose} show={true} className="modal-lg">
    <Modal.Header closeButton>
      <Modal.Title>Send Email To Enrollees Assigned {taskStableId}</Modal.Title>
      <div className="ms-4">
        {studyEnvContext.study.name}: {studyEnvContext.currentEnv.environmentName}
      </div>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()} className="py-3">
        <div>
          <label>
            Send to enrollees with task status:
            <Select
              isMulti={true}
              options={taskStatusOptions}
              value={taskStatuses
                .map(status =>
                  taskStatusOptions
                    .find(opt => opt.value === status))}
              onChange={newVals => changeTaskStatus(
                newVals
                  ?.filter(newVal => !isNil(newVal))
                  .map(newVal => newVal!.value))}/>
          </label>
        </div>

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
          Send to {searchResults.length} participants
        </button>
        <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
