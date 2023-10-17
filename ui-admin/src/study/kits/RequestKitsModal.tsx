import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import React, { useState } from 'react'
import { useKitTypeSelect } from '../participants/RequestKitModal'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import { Modal } from 'react-bootstrap'
import LoadingSpinner from 'util/LoadingSpinner'
import pluralize from 'pluralize'


/** Renders a modal for an admin to submit multiple kit requests. */
export default function RequestKitsModal({
  studyEnvContext, enrolleeShortcodes,
  onDismiss, onSubmit
}: {
    studyEnvContext: StudyEnvContextT,
    onDismiss: () => void,
    enrolleeShortcodes: string[],
    onSubmit: () => void }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(false)

  const { kitType, KitSelect } = useKitTypeSelect(portal.shortcode, study.shortcode)
  const handleSubmit = async () => {
    doApiLoad(async () => {
      const response = await Api.requestKits(portal.shortcode, study.shortcode, currentEnv.environmentName,
        enrolleeShortcodes, kitType)
      if (response.pepperApiExceptions.length) {
        const errorMessage = response.pepperApiExceptions
          .map(exception => exception.message).join('; ')
        Store.addNotification(failureNotification(
                    `${response.pepperApiExceptions.length} kit requests failed. ${errorMessage}`))
      }
      if (response.kitRequests.length) {
        Store.addNotification(successNotification(
                    `${response.pepperApiExceptions.length} kit requests created`
        ))
      }
      onSubmit()
    }, { setIsLoading })
  }
  const kitPluralized = pluralize('kit', enrolleeShortcodes.length)

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Request {kitPluralized}</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
                    Request a kit for {enrolleeShortcodes.length} enrollees
        </div>
        <div>
          <label className='form-label'>
                        Kit type
            {KitSelect}
          </label>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
        <button className='btn btn-primary' onClick={handleSubmit}>Request {kitPluralized}</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
