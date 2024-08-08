import React, { useState } from 'react'
import Api, { Trigger } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Store } from 'react-notifications-component'
import { successNotification } from 'util/notifications'
import { useUser } from 'user/UserProvider'
import { doApiLoad } from 'api/api-utils'
import { Button } from '../../components/forms/Button'
import LoadingSpinner from '../../util/LoadingSpinner'
import { StudyEnvParams } from '@juniper/ui-core'
import { isNotification } from 'study/notifications/TriggerBaseForm'


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
export default function TestEmailSender({ studyEnvParams, trigger, onDismiss }:
                                          {studyEnvParams: StudyEnvParams, onDismiss: () => void,
                                            trigger: Trigger}) {
  const { user } = useUser()
  const { portalShortcode, envName, studyShortcode } = studyEnvParams
  const [isLoading, setIsLoading] = useState(false)
  const [ruleData, setRuleData] = useState({
    ...EXAMPLE_RULE_DATA,
    profile: {
      ...EXAMPLE_RULE_DATA.profile,
      contactEmail: user?.username || ''
    }
  })
  /** sends a test email with the given (saved) notification.  does not currently reflect unsaved changes */
  function sendTestEmail() {
    doApiLoad(async () => {
      await Api.testTrigger(portalShortcode, studyShortcode, envName, trigger.id, ruleData)
      Store.addNotification(successNotification('Sent test email'))
    }, { setIsLoading })
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
      Unsaved changes will not appear. {
      // admin emails will always just go to the operator's email,
      // so we only need this prompt if it's an enrollee email
        isNotification(trigger) && 'Update the \'contactEmail\' field below to control the email destination.'
      }
      <textarea rows={20} cols={80} value={JSON.stringify(ruleData, null, 2)} onChange={updateRuleData}/>
    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary" className="btn btn-primary" onClick={sendTestEmail}
        disabled={isLoading}>{isLoading ? <LoadingSpinner/> : 'Send email'}</Button>
      <button type="button" className="ms-3 btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

