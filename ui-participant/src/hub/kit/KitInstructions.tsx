import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { KitCollectionStepWrapper, KitReturnType } from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import QRCode from 'react-qr-code'

//TODO: JN-1294, implement i18n for this entire component

export default function KitInstructions() {
  const { ppUser, enrollees } = useActiveUser()
  const { portalEnv } = usePortalEnv()
  const activeEnrollee = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)
  const isConsented = activeEnrollee?.consented
  const [selectedReturnType, setSelectedReturnType] = useState<KitReturnType>()
  //TODO: JN-1259, replace with an actual kit
  const activeKit = undefined
  const kitId = undefined

  const studySupportEmail = portalEnv.portalEnvironmentConfig.emailSourceAddress

  return <div
    className="hub-dashboard-background flex-grow-1"
    style={{ background: 'var(--dashboard-background-color)' }}
  >
    <div className="row mx-0 justify-content-center py-4">
      <div className="col-12 col-sm-10 col-lg-6">
        <div className="card-body">
          <div className="align-items-center">
            <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
              <h2 className="fw-bold pb-3">Sample Kit Instructions</h2>
              <div className="pb-3">
                If you are receiving a sample collection kit in-person, you will have the option to
                complete the collection kit now and return it immediately, or take it home and ship it
                back using the provided return label.
              </div>
              <div className="pb-3">
                If you have any questions, please ask a member of the
                study team or email <a href={`mailto:${studySupportEmail}`}>{studySupportEmail}</a>
              </div>
            </div>

            {isConsented &&
            <>
              <KitCollectionStepWrapper title={'Step 1: Confirm eligibility'} status={'COMPLETE'}>
                {activeEnrollee ? <>
                  <div className={'pb-3'}>
                    A member of the study team will scan the code below to confirm your eligibility
                    and provide you with a sample collection kit.
                  </div>
                  <div className="d-flex flex-column align-items-center">
                    <QRCode value={activeEnrollee?.shortcode} size={200}
                      className={'pb-3'} aria-label={'eligibility-qr'}/>
                  </div></> :
                  <span className="text-danger">
                    No enrollee found. Please contact a member of the study team.
                  </span>
                }
              </KitCollectionStepWrapper>

              <KitCollectionStepWrapper title={'Step 2: Complete kit'}
                status={kitId ? 'COMPLETE' : 'INCOMPLETE'}>
                <div className="mb-3">
                  A member of the study team will now provide you with a sample collection kit.
                  Complete the sample collection kit as instructed by the study team.
                </div>
                <label className="form-label fw-bold mb-0">Your unique kit identifier:</label>
                <input
                  className="my-2 form-control"
                  disabled={true}
                  placeholder={'No kit provided'}
                  value={kitId}>
                </input>
              </KitCollectionStepWrapper>

              <KitCollectionStepWrapper title={'Step 3: Select return option'}
                status={selectedReturnType ? 'COMPLETE' : 'INCOMPLETE'}>
                <div className='mb-3'>
                  Once you have completed the kit, you may return it to the study
                  team in-person, or ship it back using the provided return label.
                </div>
                <div className={'fw-bold mb-2'}>Select a return option:</div>
                <div className="form-check">
                  <input className="form-check-input" type="radio" name="returnOption" id="returnInPerson"/>
                  <label className="form-check-label" htmlFor="returnInPerson"
                    onClick={() => setSelectedReturnType('IN_PERSON')}
                  >
                    Return to study team in-person
                  </label>
                </div>
                <div className="form-check">
                  <input className="form-check-input" type="radio" name="returnOption" id="returnViaMail"/>
                  <label className="form-check-label" htmlFor="returnViaMail"
                    onClick={() => setSelectedReturnType('RETURN_LABEL')}
                  >
                    Return via mail using provided return label
                  </label>
                </div>
              </KitCollectionStepWrapper>

              {selectedReturnType === 'IN_PERSON' &&
                <KitCollectionStepWrapper title={'Step 4: Return sample'}
                  disabled={!selectedReturnType}
                  status={activeKit ? 'COMPLETE' : 'INCOMPLETE'}>
                  <div className='mb-3'>
                    A member of the study team will scan the code below along with the
                    barcode on the sample kit to confirm sample receipt.
                  </div>
                  <div className="d-flex flex-column align-items-center">
                    <QRCode value={activeEnrollee?.shortcode} size={200} className={'pb-3'}/>
                  </div>
                  <div className="pb-3">
                    You will receive an email confirmation once your sample collection has been successfully
                    associated with your account, and you can view the status of your kit on the
                    <Link to='/hub'> participant dashboard.</Link>
                  </div>
                  <div className="d-flex justify-content-center">
                    <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
                      Return to Dashboard
                    </Link>
                  </div>
                </KitCollectionStepWrapper>
              }

              {selectedReturnType === 'RETURN_LABEL' &&
                <KitCollectionStepWrapper title={'Step 4: Return sample'}
                  disabled={!selectedReturnType}
                  status={activeKit ? 'COMPLETE' : 'INCOMPLETE'}>
                  <div className='mb-3'>
                    Mail the sample collection kit back to the study team using the provided return label.
                  </div>
                  <div className="pb-3">
                    You will receive an email confirmation once your sample collection kit has been received,
                    and you can view the status of your kit on the <Link to='/hub'> participant dashboard.</Link>
                  </div>
                  <div className="d-flex justify-content-center">
                    <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
                      Return to Dashboard
                    </Link>
                  </div>
                </KitCollectionStepWrapper>
              }
            </> }
          </div>

          {!isConsented &&
            <KitCollectionStepWrapper title={'Consent Required'} status={'ERROR'}>
              <div className="mb-3">
                Before completing a sample collection kit, you must first read and sign the study consent form.
              </div>
              <div className="d-flex justify-content-center">
                <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
                  Start Consent
                </Link>
              </div>
            </KitCollectionStepWrapper>
          }
        </div>
      </div>
    </div>
  </div>
}
