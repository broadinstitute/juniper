import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api from 'api/api'
import { paramsFromContext, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Select from 'react-select'
import { ApiErrorResponse, defaultApiErrorHandle, useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import InfoPopup from 'components/forms/InfoPopup'
import { KitType, StudyEnvParams } from '@juniper/ui-core'

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
  const { skipAddressValidation, OverrideControl } = useBadAddressOverride(false)
  const handleSubmit = async () => {
    setIsLoading(true)
    try {
      await Api.createKitRequest(portal.shortcode, study.shortcode,
        currentEnv.environmentName, enrolleeShortcode, { kitType, distributionMethod: 'MAILED', skipAddressValidation })
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
          <label className="form-label mt-2">
            Kit type
            {KitSelect}
          </label>
        </div>
        { OverrideControl }
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

/**
 *
 */
export const useBadAddressOverride = (initialValue: boolean) => {
  const [skipAddressValidation, setSkipAddressValidation] = useState(initialValue)

  const OverrideControl = <div className="d-flex align-items-center">
    <label className="form-label mt-2">
      <input type="checkbox" checked={skipAddressValidation}
        onChange={e => setSkipAddressValidation(e.target.checked)}
      /> Override address validation
    </label>
    <InfoPopup content="Checking this box will create the kit
            request even if the address does not pass mailing address validation."/>
  </div>
  return { skipAddressValidation, setSkipAddressValidation, OverrideControl }
}
