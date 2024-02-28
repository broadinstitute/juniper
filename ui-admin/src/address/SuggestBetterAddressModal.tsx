import { MailingAddress } from '../api/api'
import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { findDifferencesBetweenObjects } from '../../../ui-core/src/objectUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'


const irrelevantFields = ['createdAt', 'lastUpdatedAt', 'id']
/**
 * Suggests an improved address which the user may override or accept.
 */
export default function SuggestBetterAddressModal(
  {
    inputtedAddress,
    improvedAddress,
    hasInferredComponents,
    accept,
    deny,
    onDismiss
  } : {
    inputtedAddress: MailingAddress,
    improvedAddress: MailingAddress,
    hasInferredComponents: boolean,
    accept: () => void,
    deny: () => void,
    onDismiss: () => void
  }
) {
  const changes = findDifferencesBetweenObjects(inputtedAddress, improvedAddress)
    .filter(diff => !irrelevantFields.includes(diff.fieldName))

  return <Modal show={true} size={'lg'}>
    <Modal.Header>
      <Modal.Title>
        Is this the correct address?
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>
        Please verify the improvements made to the address.
        {hasInferredComponents ? ' Some of these changes are required for a valid address.' : ''}
      </p>
      <div className="border-start border-3 p-1 ps-2 border-info w-75 ms-4 mb-4"
        style={{ backgroundColor: '#f2f2f2' }}>
        <p className={'fw-bold mb-0'}>Pending Changes</p>
        {changes.map((change, idx) =>
          <p key={idx} className="mb-0">
            {change.fieldName}: {change.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {change.newValue}
          </p>
        )}
      </div>
      <div className="d-flex">
        <button className="btn btn-primary" onClick={accept}>Yes</button>
        <button className="btn btn-secondary" onClick={deny}>No</button>
        <div className="flex-grow-1">
          <button className="float-end btn btn-secondary" onClick={onDismiss}>Cancel</button>
        </div>
      </div>
    </Modal.Body>
  </Modal>
}
