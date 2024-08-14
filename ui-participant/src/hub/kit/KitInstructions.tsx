import React, { lazy, ReactNode, useState } from 'react'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleCheck, faCircleDot, faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { useActiveUser } from 'providers/ActiveUserProvider'
const QRCode = lazy(() => import('react-qr-code'))

export function KitInstructions() {
  const { ppUser, enrollees } = useActiveUser()
  const activeEnrollee = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)
  const isConsented = activeEnrollee?.consented
  const kitId = '74fceed84fe3494fbb2b9976b13d815e'
  const [selectedReturnType, setSelectedReturnType] = useState<KitReturnType>()
  const activeKit = undefined
  const activeKitStatus: KitStatus = 'SENT'

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
                study team or email <a href="mailto:info@ourhealthstudy.org">info@ourhealthstudy.org</a>
              </div>
            </div>

            {isConsented &&
              <>
                <KitCollectionStep title={'Step 1: Confirm eligibility'} status={'COMPLETE'}>
                  {activeEnrollee ? <>
                    <div className={'pb-3'}>
                      A member of the study team will scan the code below to confirm your eligibility
                      and provide you with a sample collection kit.
                    </div>
                    <div className="d-flex flex-column align-items-center">
                      <QRCode value={activeEnrollee?.shortcode} size={200} className={'pb-3'}/>
                    </div></> :
                    <span className="text-danger">
                      No enrollee found. Please contact a member of the study team.
                    </span>
                  }
                </KitCollectionStep>

                <KitCollectionStep title={'Step 2: Complete kit'}
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
                </KitCollectionStep>

                <KitCollectionStep title={'Step 3: Select return option'}
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
                </KitCollectionStep>

                {selectedReturnType === 'IN_PERSON' &&
                  <KitCollectionStep title={'Step 4: Return sample'}
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
                  </KitCollectionStep>
                }

                {selectedReturnType === 'RETURN_LABEL' &&
                  <KitCollectionStep title={'Step 4: Return sample'}
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
                  </KitCollectionStep>
                }
              </> }
          </div>

          {!isConsented &&
            <KitCollectionStep title={'Consent Required'} status={'ERROR'}>
              <div className="mb-3">
                Before completing a sample collection kit, you must first read and sign the study consent form.
              </div>
              <div className="d-flex justify-content-center">
                <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
                  Start Consent
                </Link>
              </div>
            </KitCollectionStep>
          }
        </div>
      </div>
    </div>
  </div>
}

type KitStatus = 'SENT' | 'RECEIVED'
type KitReturnType = 'IN_PERSON' | 'RETURN_LABEL'
type StepStatus = 'COMPLETE' | 'INCOMPLETE' | 'ERROR'

const stepStatusToIcon = (status: StepStatus) => {
  switch (status) {
    case 'COMPLETE':
      return <FontAwesomeIcon className="text-success me-1" icon={faCircleCheck}/>
    case 'INCOMPLETE':
      return <FontAwesomeIcon className="text-muted me-1" icon={faCircleDot}/>
    case 'ERROR':
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleExclamation}/>
  }
}

const KitCollectionStep = ({ title, status, children, disabled }: {
  title: string, status: StepStatus, children: ReactNode, disabled?: boolean
}) => {
  return <div className="mb-3 rounded round-3 border border-1 p-3 bg-white"
    style={disabled ? { filter: 'brightness(33%)' } : {}}>
    <h2 className="d-flex align-items-center mb-3">
      { stepStatusToIcon(status) } {title}
    </h2>
    <div className='mb-2'>{children}</div>
  </div>
}
