import React, { useState } from 'react'
import Api, { NotificationConfig } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'


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

/** Sends test emails based on a configurable profile */
export default function TestEmailSender({ portalShortcode, environmentName, notificationConfig, show, setShow }:
                                          {portalShortcode: string, environmentName: string,
                                            show: boolean, setShow: (show: boolean) => void,
                                            notificationConfig: NotificationConfig}) {
  const [ruleData, setRuleData] = useState(exampleRuleData)
  /** sends a test email with the given (saved) notification.  does not currently reflect unsaved changes */
  function sendTestEmail() {
    Api.testNotification(portalShortcode, environmentName, notificationConfig.id, ruleData).then(() =>
      Store.addNotification(successNotification(
        'Sent test email'
      ))).catch(() =>
      Store.addNotification(failureNotification(
        'error sending test email '
      ))
    )
  }

  /** updates the data that will be used to generate the fake email */
  function updateRuleData(event: React.ChangeEvent<HTMLTextAreaElement>) {
    try {
      const newData = JSON.parse(event.currentTarget.value)
      setRuleData(newData)
    } catch (e) {
      alert(`Could not parse: ${ e}`)
    }
  }

  return <Modal show={show} onHide={() => setShow(false)} className="modal-lg">
    <Modal.Header closeButton>
      <Modal.Title>Send test email</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <textarea rows={20} cols={80} value={JSON.stringify(ruleData, null, 2)} onChange={updateRuleData}/>
    </Modal.Body>
    <Modal.Footer>
      <button type="button" className="btn btn-primary" onClick={sendTestEmail}>Send email</button>
      <button type="button" className="ms-3 btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

