import React from 'react'
import InfoPopup from 'components/forms/InfoPopup'
import { Placement } from 'react-bootstrap/types'
import {
  KitRequest,
  KitRequestStatus
} from '@juniper/ui-core'
import { prettifyString } from './KitRequests'

/**
 * Component to render the currentStatus as reported by Pepper, including some help text to explain what the statuses
 * mean and include error detail for error status.
 */
export default function KitStatusCell(
  { kitRequest, infoPlacement = 'top' }:
  { kitRequest: KitRequest, infoPlacement?: Placement }
) {
  const infoTexts: Record<KitRequestStatus, string> = {
    'NEW': 'Kit request received, has not yet shipped',
    'CREATED': 'Kit request received, has not yet shipped',
    'QUEUED': 'Shipping label has been printed',
    'SENT': 'Kit has been sent to the participant',
    'RECEIVED': 'Kit has been returned by the participant',
    'COLLECTED_BY_STAFF': 'Participant has returned kit to study staff in-person',
    'SENT_BY_STAFF': 'Kit was manually sent to the participant by study staff',
    'ERRORED': 'There was a problem fulfilling this kit request',
    'UNKNOWN': 'An unknown problem occurred.',
    'DEACTIVATED': 'Kit is deactivated - no further processing will be done'
  }
  const currentStatus = kitRequest.status
  const info = currentStatus ? infoTexts[currentStatus] : ''
  const errorMessage = kitRequest.errorMessage ? `: ${kitRequest.errorMessage}` : ''
  const content = `${info}${errorMessage}`
  return <>
    {prettifyString(kitRequest.status)}
    {info && <InfoPopup content={content} placement={infoPlacement}/>}
  </>
}
