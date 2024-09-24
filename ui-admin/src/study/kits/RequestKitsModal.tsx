import { paramsFromContext, StudyEnvContextT } from '../StudyEnvironmentRouter'
import React, { useState } from 'react'
import { useBadAddressOverride, useKitTypeSelect } from '../participants/RequestKitModal'
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
    onSubmit: (anyKitWasCreated: boolean) => void }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(false)

  const { kitType, KitSelect } = useKitTypeSelect(paramsFromContext(studyEnvContext))
  const { skipAddressValidation, OverrideControl } = useBadAddressOverride(false)
  const handleSubmit = async () => {
    doApiLoad(async () => {
      const response = await Api.requestKits(portal.shortcode, study.shortcode, currentEnv.environmentName,
        enrolleeShortcodes, { kitType, distributionMethod: 'MAILED', skipAddressValidation })
      if (response.exceptions.length) {
        const errorMessage = response.exceptions
          .map(exception => exception.message).join('; ')
        Store.addNotification(failureNotification(
                    `${response.exceptions.length} kit requests failed. ${errorMessage}`))
      }
      if (response.kitRequests.length) {
        Store.addNotification(successNotification(
                    `${response.kitRequests.length} kit requests created`
        ))
      }
      onSubmit(!!response.kitRequests.length)
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
          { OverrideControl}
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
