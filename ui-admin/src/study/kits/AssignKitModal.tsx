import {
  paramsFromContext,
  StudyEnvContextT
} from '../StudyEnvironmentRouter'
import React, { useState } from 'react'
import { useKitTypeSelect } from '../participants/RequestKitModal'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import { Modal } from 'react-bootstrap'
import LoadingSpinner from 'util/LoadingSpinner'
import { Enrollee } from '@juniper/ui-core'
import { isEmpty } from 'lodash'

type ManualKitRequestCreationDto = {
  distributionMethod: 'MANUAL',
  kitType: string,
  kitLabel: string,
  skipAddressValidation: boolean,
  returnTrackingNumber?: string,
}
/** Renders a modal for an admin to quickly scan & send one or more kit requests. */
export default function AssignKitModal({
  studyEnvContext, enrollee,
  onDismiss, onSubmit
}: {
  studyEnvContext: StudyEnvContextT,
  onDismiss: () => void,
  enrollee: Enrollee,
  onSubmit: (anyKitWasCreated: boolean) => void
}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(false)

  const [kitLabel, setKitLabel] = useState<string>()
  const [returnTrackingNumber, setReturnTrackingNumber] = useState<string>()


  const { kitType, KitSelect } = useKitTypeSelect(paramsFromContext(studyEnvContext))

  const assembledKitDto: ManualKitRequestCreationDto = {
    kitType,
    distributionMethod: 'MANUAL',
    kitLabel: kitLabel || '',
    skipAddressValidation: false,
    returnTrackingNumber
  }
  // only required field is kitLabel
  const isKitComplete = !isEmpty(assembledKitDto.kitLabel)


  const handleSubmit = async () => {
    doApiLoad(async () => {
      const response = await Api.createKitRequest(
        portal.shortcode,
        study.shortcode,
        currentEnv.environmentName,
        enrollee.shortcode,
        assembledKitDto)
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


  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Assign Kit</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div>
        <p>
          Please confirm profile information:
        </p>
        <p>Shortcode: {enrollee.shortcode}</p>
        <p>Full name: {enrollee.profile.givenName} {enrollee.profile.familyName}</p>
        <p>Sex at birth: {enrollee.profile.sexAtBirth}</p>
      </div>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          <label className='form-label'>
            Kit type
            {KitSelect}
          </label>
        </div>
        <div>

        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
        <button className='btn btn-primary' onClick={handleSubmit} disabled={!isKitComplete}>
          Assign kit
        </button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
