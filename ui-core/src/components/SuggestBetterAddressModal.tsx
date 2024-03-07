import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { MailingAddress } from '../types/address'
import { ModalProps } from 'react-bootstrap'
import FormattedAddress from '../components/FormattedAddress'
import { isNil } from 'lodash'


/**
 * Suggests an improved address which the user may override or accept.
 */
export default function SuggestBetterAddressModal(
  {
    inputtedAddress,
    improvedAddress,
    accept,
    reject,
    onDismiss,
    goBack,
    animated,
    ModalComponent = Modal
  } : {
    inputtedAddress: MailingAddress,
    improvedAddress: MailingAddress,
    accept: () => void,
    reject: () => void,
    onDismiss: () => void,
    goBack?: () => void,
    animated?: boolean,
    ModalComponent?: React.ElementType<ModalProps>
  }
) {
  const [acceptedSuggestedAddress, setAcceptedSuggestedAddress] = useState<boolean>(true)

  return <ModalComponent show={true} animation={animated} onHide={onDismiss}>
    <Modal.Header>
      <Modal.Title>
        Is this the correct address?
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>
        We were able to verify a similar address. Please select which you would like to use.
      </p>
      <div className="ps-4">
        <div>
          <p className="fw-bold mb-0 mt-1">New Address</p>
          <div className="form-check d-inline-block p-1">
            <input
              className="form-check-input"
              type="radio"
              name="suggestedAddressRadioGroup"
              id="suggestedAddressRadio"
              onChange={() => setAcceptedSuggestedAddress(true)}
              checked={acceptedSuggestedAddress}
            />
            <label className="form-check-label" htmlFor="suggestedAddressRadio">
              <FormattedAddress
                address={improvedAddress}
                showDiff={inputtedAddress}
              />
            </label>
          </div>
        </div>

        <div>
          <p className="fw-bold mb-0 mt-1">Old Address</p>
          <div className="form-check d-inline-block p-1">
            <input
              className="form-check-input"
              type="radio"
              name="suggestedAddressRadioGroup"
              id="oldAddressRadio"
              onChange={() => setAcceptedSuggestedAddress(false)}
              checked={!acceptedSuggestedAddress}
            />
            <label className="form-check-label" htmlFor="oldAddressRadio">
              <FormattedAddress address={inputtedAddress}/>
            </label>
          </div>
        </div>

      </div>
    </Modal.Body>
    <Modal.Footer>
      <div className="d-flex flex-row justify-content-start w-100">
        {isNil(goBack)
          ? <button className="btn btn-outline-secondary" onClick={onDismiss}>Cancel</button>
          : <button className="btn btn-outline-secondary" onClick={goBack}>Go Back</button>
        }

        <div className="flex-grow-1">
          <button className="float-end btn btn-primary" onClick={() => {
            if (acceptedSuggestedAddress) {
              accept()
            } else {
              reject()
            }
          }}>Continue
          </button>
        </div>
      </div>
    </Modal.Footer>
  </ModalComponent>
}
