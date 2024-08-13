import React from 'react'
import QRCode from 'react-qr-code'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBox, faTruck, faVials } from '@fortawesome/free-solid-svg-icons'

export function KitInstructions() {
  const isConsented = true
  const isProvidingSample = true
  // const isCollectingSample = true

  return <div
    className="hub-dashboard-background flex-grow-1"
    style={{ background: 'var(--dashboard-background-color)' }}
  >
    <div className="row mx-0 justify-content-center py-4">
      <div className="card mb-3 w-100">
        <div className="card-body p-4">
          {isConsented && isProvidingSample &&
              <div className="align-items-center">
                <h2 className="fw-bold pb-3">Sample Kit Instructions</h2>
                <div className="pb-3">
                      If you are receiving a sample collection kit in-person, you will have the option to
                      complete the
                      collection kit now and return it immediately, or take it home and ship it back using the
                      provided
                      return label.
                </div>
                <div className="pb-3">
                      If you have any questions, please ask a member of the study team.
                </div>
                <div className="progress mb-3" style={{ height: '50px' }}>
                  <div className="progress-bar" role="progressbar" style={{
                    width: '33%',
                    color: 'var(--brand-color-shift-90)',
                    backgroundColor: 'var(--brand-color)'
                  }}
                  aria-valuenow="33"
                  aria-valuemin="0"
                  aria-valuemax="100">
                    <FontAwesomeIcon icon={faBox} className={'h-50'}/>
                      Kit Provided
                  </div>

                  <div className="progress-bar border-left border-1" role="progressbar" style={{
                    width: '34%',
                    color: 'var(--brand-color)',
                    backgroundColor: 'var(--brand-color-shift-20)'
                  }}
                  aria-valuenow="34"
                  aria-valuemin="0"
                  aria-valuemax="100">
                    <FontAwesomeIcon icon={faVials} className={'h-50'}/>
                  </div>

                  <div className="progress-bar bg-info" role="progressbar" style={{ width: '33%' }} aria-valuenow="33"
                    aria-valuemin="0" aria-valuemax="100">
                    <FontAwesomeIcon icon={faTruck} className={'h-50'}/>

                  </div>
                </div>
                <h4>Step 1: Confirm eligibility</h4>
                <div className="pb-4">
                    A member of the study team will scan your QR code below to confirm your eligibility
                    and provide you with a sample collection kit.
                </div>
                <div className="d-flex flex-column align-items-center">
                  <QRCode value={'OHSALK'} size={200} className={'pb-3'}/>
                </div>
                <h4>Step 2: Complete sample collection</h4>
                <div className="pb-4">
                      After you have consented to the study, a member of
                      the study team will provide you with a sample collection kit.
                </div>

                <h4>Step 3</h4>
                <div className="pb-4">
                      Complete the sample collection kit as instructed by the study team and return it to them
                      when you are finished.
                </div>
                <h4>Step 4</h4>
                <div className="pb-4">
                      A member of the study team will scan the QR code below to confirm your sample collection
                      and
                      associate it with your account.
                </div>
                <div className="d-flex flex-column align-items-center">
                  <QRCode value={'OHSALK'} size={200} className={'pb-3'}/>
                </div>
                <h4>Step 5</h4>
                <div className="pb-3">
                      You will receive an email confirmation once your sample collection has been successfully
                      associated with your account, and you can view the status of your kit on the
                  <Link to='/hub'> participant dashboard.</Link>
                </div>
              </div>}
          {!isConsented && <div className="align-items-center">
            <h2 className="fw-bold pb-3">Consent Required</h2>
            <div className="pb-3">
                    Before completing a sample collection kit, you must first read and sign the study consent
                    form.
            </div>
            <div className="d-flex flex-column align-items-center">
              <Link
                to={'/hub'}
                className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary"
              >
                    Start Consent
              </Link>
            </div>
          </div>
          }
        </div>
      </div>
    </div>
  </div>
}

// const KitProgressMeter = ({ isConsented, isProvidingSample, isCollectingSample }: {
//
// }
