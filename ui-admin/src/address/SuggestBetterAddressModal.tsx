import { MailingAddress } from '../api/api'
import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { findDifferencesBetweenObjects } from '../util/objectUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'


const irrelevantFields = ['createdAt', 'lastUpdatedAt', 'id']
/**
 *
 */
export default function SuggestBetterAddressModal(
  {
    inputtedAddress,
    improvedAddress,
    accept,
    deny,
    onDismiss
  } : {
    inputtedAddress: MailingAddress,
    improvedAddress: MailingAddress,
    accept: () => void,
    deny: () => void,
    onDismiss: () => void
  }
) {
  const changes = findDifferencesBetweenObjects(inputtedAddress, improvedAddress)
    .filter(diff => !irrelevantFields.includes(diff.fieldName))

  return <Modal show={true}>
    <Modal.Header>
      <Modal.Title>
        Is this your address?
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>
        We think there might be some improvements which could be made to your address. Would you like to accept them?
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
