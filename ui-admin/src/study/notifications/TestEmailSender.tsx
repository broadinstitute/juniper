import React, { useState } from 'react'
import Api, { NotificationConfig } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import { useUser } from '../../user/UserProvider'


export const EXAMPLE_RULE_DATA = {
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
export default function TestEmailSender({ portalShortcode, environmentName, notificationConfig, onDismiss }:
                                          {portalShortcode: string, environmentName: string, onDismiss: () => void,
                                            notificationConfig: NotificationConfig}) {
  const { user } = useUser()
  const [ruleData, setRuleData] = useState({
    ...EXAMPLE_RULE_DATA,
    profile: {
      ...EXAMPLE_RULE_DATA.profile,
      contactEmail: user.username
    }
  })
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

  return <Modal show={true} onHide={onDismiss} className="modal-lg">
    <Modal.Header closeButton>
      <Modal.Title>Send test email</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      This will send a test email using the participant data below with the most recently saved version of the template.
      Unsaved changes will not appear. Update the &apos;contactEmail&apos; field below to control the email desitnation.
      <textarea rows={20} cols={80} value={JSON.stringify(ruleData, null, 2)} onChange={updateRuleData}/>
    </Modal.Body>
    <Modal.Footer>
      <button type="button" className="btn btn-primary" onClick={sendTestEmail}>Send email</button>
      <button type="button" className="ms-3 btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

