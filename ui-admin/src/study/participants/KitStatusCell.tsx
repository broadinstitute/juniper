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
    'kit without label': 'Kit request has been received',
    'queue': 'Shipping label has been printed',
    'sent': 'Kit has been sent to the participant',
    'received': 'Kit has been returned by the participant',
    'error': 'There was a problem fulfilling this kit request',
    'deactivated': 'Kit is deactivated'
  }
  const currentStatus = kitRequest.pepperStatus?.currentStatus
  const info = currentStatus ? infoTexts[currentStatus.toLowerCase()] : ''
  const errorMessage = kitRequest.pepperStatus?.errorMessage ? `: ${kitRequest.pepperStatus.errorMessage}` : ''
  const content = `${info}${errorMessage}`
  return <>
    {kitRequest.pepperStatus?.currentStatus}
    {info && <InfoPopup content={content} placement={infoPlacement}/>}
  </>
}
