import React from 'react'
import { KitRequest } from 'api/api'
import InfoPopup from 'components/forms/InfoPopup'
import { Placement } from 'react-bootstrap/types'

/**
 * Component to render the currentStatus as reported by Pepper, including some help text to explain what the statuses
 * mean and include error detail for error status.
 */
export default function KitStatusCell(
  { kitRequest, infoPlacement = 'top' }:
  { kitRequest: KitRequest, infoPlacement?: Placement }
) {
  const infoTexts: Record<string, string> = {
    'CREATED': 'Kit request has been received',
    'QUEUE': 'Shipping label has been printed',
    'SENT': 'Kit has been sent to the participant',
    'RECEIVED': 'Kit has been returned by the participant',
    'ERRORED': 'There was a problem fulfilling this kit request',
    'DEACTIVATED': 'Kit is deactivated - no further processing will be done'
  }
  const currentStatus = kitRequest.status
  const info = currentStatus ? infoTexts[currentStatus] : ''
  const errorMessage = kitRequest.parsedExternalRequest?.errorMessage ?
      `: ${kitRequest.parsedExternalRequest.errorMessage}` : ''
  const content = `${info}${errorMessage}`
  return <>
    {kitRequest.status}
    {info && <InfoPopup content={content} placement={infoPlacement}/>}
  </>
}
