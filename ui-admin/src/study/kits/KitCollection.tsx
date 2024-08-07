import React, { ReactNode, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { Button } from 'components/forms/Button'
import { Scanner } from '@yudiel/react-qr-scanner'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { dateToDefaultString, Enrollee } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircle, faCircleCheck, faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from '../../components/forms/InfoPopup'


export const KitCollection = ({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) => {
  const [showEnrolleeCodeScanner, setShowEnrolleeCodeScanner] = useState(false)
  const [showKitScanner, setShowKitScanner] = useState(false)
  const [enrollee, setEnrollee] = useState<Enrollee>()
  const [confirmedIdentity, setConfirmedIdentity] = useState(false)
  const [qrError, setQrError] = useState<string>()
  const [kitId, setKitId] = useState<string>()

  const loadEnrollee = (enrolleeShortcode: string) => {
    doApiLoad(async () => {
      const loadedEnrollee = await Api.getEnrollee(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrolleeShortcode
      )
      if (loadedEnrollee.consented) { setEnrollee(loadedEnrollee) } else {
        setQrError('Enrollee has not consented to the study'
        )
      }
    })
  }

  return <div className='m-2'>
    {renderPageHeader('Collect a kit')}
    <div className={'text-muted mb-1'}>To collect an in-person kit, follow the steps below. Please ensure that
    all information is correct before submitting.</div>
    <KitCollectionStep stepNumber={1} stepTitle={
      <>
        Click to open the camera and scan the enrollee&apos;s barcode
        <InfoPopup content={'The enrollee can find their unique QR code by going to their profile'}/>
      </>} stepStatus={qrError ? 'error' :
      enrollee ? 'complete' : 'incomplete'
    }>
      <Button className="mb-2" variant={'primary'} onClick={() => {
        setShowEnrolleeCodeScanner(!showEnrolleeCodeScanner)
      }}>
        Click to scan enrollee code
      </Button>
      { showEnrolleeCodeScanner && <div style={{ width: '80%', height: '80%' }}>
        <Scanner formats={['qr_code']} components={{ audio: false }} scanDelay={2500} onScan={result => {
          if (result.length > 1) {
            setQrError('Multiple barcodes detected. Please scan again with only one barcode visible.')
          }  else {
            setQrError(undefined)
            loadEnrollee(result[0].rawValue)
          }
          setShowEnrolleeCodeScanner(false)
        }}/>
      </div> }
      { qrError && <div className="text-danger">{qrError}</div> }
    </KitCollectionStep>
    <KitCollectionStep stepNumber={2} stepTitle={<>Confirm the enrollee&apos;s identity</>}
      stepStatus={confirmedIdentity ? 'complete' : 'incomplete'}>
      <div>
        <TextInput className="mb-1" disabled={true}
          value={enrollee ?
            `${enrollee.profile.givenName} ${enrollee.profile.familyName}` :
            ''
          }>
        </TextInput>
        <TextInput className="mb-2" disabled={true}
          value={enrollee ?
            dateToDefaultString(enrollee.profile.birthDate) :
            ''
          }>
        </TextInput>
        <Button className="mb-2" variant={'primary'}
          disabled={!enrollee} onClick={() => setConfirmedIdentity(true)}>
          Mark as confirmed
        </Button>
      </div>

    </KitCollectionStep>
    <KitCollectionStep stepNumber={3} stepTitle={'Click to open the camera and scan the kit barcode'}
      stepStatus={kitId ? 'complete' : 'incomplete'}>
      <Button className="mb-2" variant={'primary'} disabled={!confirmedIdentity}
        tooltip={!confirmedIdentity ? 'You must complete the prior steps first' : ''}
        onClick={() => setShowKitScanner(!showKitScanner)}>
        Click to scan kit barcode
      </Button>
      { showKitScanner && <div style={{ width: '80%', height: '80%' }}>
        <Scanner formats={['upc_a']} components={{ audio: false }} onScan={result => {
          if (result.length > 1) {
            console.log('multiple barcodes detected!')
          } else {
            setKitId(result[0].rawValue)
            setShowKitScanner(false)
          }
        }}/>
      </div>
      }
      <TextInput className="mb-2" disabled={true} value={kitId}></TextInput>
    </KitCollectionStep>
    {/*<div className="text-muted">Please ensure that*/}
    {/*  all of the details above are correct before submitting the kit.*/}
    {/*</div>*/}
    <div className="d-flex justify-content-end">
      <Button disabled={!(kitId && enrollee)} variant={'primary'}>Submit kit</Button>
    </div>
  </div>
}

type StepStatus = 'complete' | 'incomplete' | 'error'

const stepStatusToIcon = (status: StepStatus) => {
  switch (status) {
    case 'complete':
      return <FontAwesomeIcon className="text-success me-1" icon={faCircleCheck}/>
    case 'incomplete':
      return <FontAwesomeIcon className="text-muted me-1" icon={faCircle}/>
    case 'error':
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleExclamation}/>
  }
}

const KitCollectionStep = ({ stepNumber, stepTitle, stepStatus, children }: {
    stepNumber: number, stepTitle: ReactNode, stepStatus: StepStatus, children: ReactNode
}) => {
  return <div className="mb-3 rounded round-3 border border-1 p-3">
    <h2 className="d-flex align-items-center mb-3">
      { stepStatusToIcon(stepStatus) } Step {stepNumber}
    </h2>
    <div className='mb-2'>{stepTitle}</div>
    {children}
  </div>
}
