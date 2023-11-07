import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { KitType } from 'api/api'
import { paramsFromContext, StudyEnvContextT, StudyEnvParams } from 'study/StudyEnvironmentRouter'
import Select from 'react-select'
import { ApiErrorResponse, defaultApiErrorHandle, useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function RequestKitModal({
  studyEnvContext, enrolleeShortcode,
  onDismiss, onSubmit
}: {
    studyEnvContext: StudyEnvContextT,
    onDismiss: () => void,
    enrolleeShortcode: string,
    onSubmit: () => void }) {
  const { portal, study, currentEnv } = studyEnvContext
  const [isLoading, setIsLoading] = useState(false)
  const { kitType, KitSelect } = useKitTypeSelect(paramsFromContext(studyEnvContext))
  const handleSubmit = async () => {
    setIsLoading(true)
    try {
      await Api.createKitRequest(portal.shortcode, study.shortcode,
        currentEnv.environmentName, enrolleeShortcode, kitType)
      Store.addNotification(successNotification('Kit request created'))
      onSubmit()
    } catch (e) {
      if ((e as ApiErrorResponse).message.includes('ADDRESS_VALIDATION_ERROR')) {
        Store.addNotification(failureNotification(`
          Could not create kit request:  Address did not match any mailable address.\n\n  
          The Participant will need to update their address in order to receive a kit`))
      } else {
        defaultApiErrorHandle(e as ApiErrorResponse)
      }
      onDismiss()
    }
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Request a kit</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          Enrollee: {enrolleeShortcode}
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
        <button className='btn btn-primary' onClick={handleSubmit}>Request Kit</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

/** hook for a kit type selector that handles loading the kit type options from the server.
 * returns the selected type and the KitSelect component to render */
export const useKitTypeSelect = (studyEnvParams: StudyEnvParams) => {
  const [kitTypes, setKitTypes] = useState<KitType[]>()
  const [kitType, setKitType] = useState('')
  const kitTypeOptions = kitTypes?.map(kitType => ({ label: kitType.displayName, value: kitType.name }))
  const selectedKitTypeOption = kitTypeOptions?.find(kitTypeOption => kitTypeOption.value === kitType)

  useLoadingEffect(async () => {
    const fetchedKitTypes = await Api.fetchKitTypes(studyEnvParams)
    setKitTypes(fetchedKitTypes)
    kitType || setKitType(fetchedKitTypes[0]?.name)
  })

  return {
    kitType, KitSelect: <Select options={kitTypeOptions} value={selectedKitTypeOption}
      onChange={option => setKitType(option?.value ?? '')}/>
  }
}
