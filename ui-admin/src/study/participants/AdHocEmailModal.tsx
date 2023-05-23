import React, {useEffect, useState} from 'react'
import {StudyEnvContextT} from "../StudyEnvironmentRouter";
import Modal from "react-bootstrap/Modal";
import LoadingSpinner from "util/LoadingSpinner";
import Api, {NotificationConfig} from "api/api";
import {failureNotification} from "util/notifications";
import {Store} from "react-notifications-component";
import Select from 'react-select'

export default function AdHocEmailModal({enrolleeShortcodes, onDismiss, studyEnvContext}:
{enrolleeShortcodes: string[], studyEnvContext: StudyEnvContextT, onDismiss: () => void}) {
  const [isLoading, setIsLoading] = useState(true)
  const [configs, setConfigs] = useState<NotificationConfig[]>([])
  const [selectedConfig, setSelectedConfig] = useState<NotificationConfig | null>(null)
  const [adHocMessage, setAdHocMessage] = useState('')
  const [adHocSubject, setAdHocSubject] = useState('')
  useEffect(() => {
    Api.findNotificationConfigsForStudyEnv(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName).then(result => {
        setConfigs(result)
        setIsLoading(false)
    }).catch(() => {
      Store.addNotification(failureNotification('Could not load notification configs'))
    })
  }, [])

  const sendEmail = () => {
    if (!selectedConfig) {
      return
    }
    Api.sendAdHocNotification({
      portalShortcode: studyEnvContext.portal.shortcode,
      studyShortcode: studyEnvContext.study.shortcode,
      envName: studyEnvContext.currentEnv.environmentName,
      enrolleeShortcodes,
      customMessages: {adHocMessage, adHocSubject},
      notificationConfigId: selectedConfig.id
    })
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
        <label>Email template:
          <Select options={configs} value={selectedConfig} onChange={opt => setSelectedConfig(opt)}
                getOptionLabel={config => config.emailTemplate.name}
                  getOptionValue={config => config.id}
                  styles={{control: (baseStyles) => ({...baseStyles, width: '400px'})}}/>
        </label>
        { selectedConfig?.notificationType === 'AD_HOC' &&
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
          Send to {enrolleeShortcodes.length} participants
        </button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
