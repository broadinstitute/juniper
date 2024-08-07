import React, { ReactNode, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { Button } from 'components/forms/Button'
import { IDetectedBarcode, IScannerProps, Scanner } from '@yudiel/react-qr-scanner'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { dateToDefaultString, Enrollee } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleCheck, faCircleDot, faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from 'components/forms/InfoPopup'


export const KitCollection = ({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) => {
  const [showEnrolleeCodeScanner, setShowEnrolleeCodeScanner] = useState(false)
  const [showKitScanner, setShowKitScanner] = useState(false)
  const [enrollee, setEnrollee] = useState<Enrollee>()
  const [confirmedIdentity, setConfirmedIdentity] = useState(false)
  const [qrError, setQrError] = useState<string>()
  const [kitError, setKitError] = useState<string>()
  const [kitId, setKitId] = useState<string>()

  const loadEnrollee = (enrolleeShortcode: string) => {
    doApiLoad(async () => {
      const loadedEnrollee = await Api.getEnrollee(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrolleeShortcode
      )
      if (loadedEnrollee.consented) {
        setEnrollee(loadedEnrollee)
      } else {
        setQrError('Enrollee has not consented to the study')
        setEnrollee(undefined)
      }
    })
  }

  return <div className='m-2 vh-100' style={{ maxWidth: '450px' }}>
    {renderPageHeader('Collect a kit')}
    <div className={'text-muted mb-1'}>
      To collect an in-person kit, follow the steps below. Please ensure that
      all information is correct before submitting.
    </div>

    <KitCollectionStep
      stepNumber={1}
      description={<>
        Click to open the camera and scan the enrollee&apos;s barcode
        <InfoPopup content={'The enrollee can find their unique QR code by going to their profile'}/>
      </>
      }
      status={qrError ? 'ERROR' : enrollee ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <Button className="mb-2" variant={'primary'} onClick={() => {
        setShowEnrolleeCodeScanner(!showEnrolleeCodeScanner)
      }}>
        Click to scan enrollee code
      </Button>
      { showEnrolleeCodeScanner &&
        <BarcodeScanner
          expectedFormats={['qr_code']}
          onError={error => {
            setQrError(error)
            setEnrollee(undefined)
          }}
          onSuccess={result => {
            loadEnrollee(result.rawValue)
            setShowEnrolleeCodeScanner(false)
          }}/>
      }
      { qrError &&
          <div className="text-danger">{qrError}</div>
      }
    </KitCollectionStep>

    <KitCollectionStep
      stepNumber={2}
      status={confirmedIdentity ? 'COMPLETE' : 'INCOMPLETE'}
      description={<>Confirm the enrollee&apos;s identity</>}
    >
      <div>
        <TextInput
          label={'Enrollee Name'}
          className="mb-1" disabled={true}
          value={enrollee ?
            `${enrollee.profile.givenName} ${enrollee.profile.familyName}` :
            ''
          }>
        </TextInput>
        <TextInput
          label={'Date of Birth'}
          className="mb-2" disabled={true}
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

    <KitCollectionStep
      stepNumber={3}
      description={'Click to open the camera and scan the kit barcode'}
      status={kitId ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <Button className="mb-2" variant={'primary'} disabled={!confirmedIdentity}
        tooltip={!confirmedIdentity ? 'You must complete the prior steps first' : ''}
        onClick={() => setShowKitScanner(!showKitScanner)}>
        Click to scan kit barcode
      </Button>
      { showKitScanner &&
          <BarcodeScanner
            expectedFormats={['upc_a']}
            onError={error => setKitError(error)}
            onSuccess={result => {
              setKitId(result.rawValue)
              setShowKitScanner(false)
            }}/>
      }
      <TextInput
        className="my-2"
        disabled={true}
        value={kitId}>
      </TextInput>
      { kitError &&
          <div className="text-danger">{kitError}</div>
      }
    </KitCollectionStep>
    <div className="d-flex justify-content-end">
      <Button disabled={!(kitId && enrollee)} variant={'primary'}>Submit kit</Button>
    </div>
  </div>
}

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

const KitCollectionStep = ({ stepNumber, status, description, children }: {
  stepNumber: number, status: StepStatus, description: ReactNode, children: ReactNode
}) => {
  return <div className="mb-3 rounded round-3 border border-1 p-3">
    <h2 className="d-flex align-items-center mb-3">
      { stepStatusToIcon(status) } Step {stepNumber}
    </h2>
    <div className='mb-2'>{description}</div>
    {children}
  </div>
}

const BarcodeScanner = ({ expectedFormats, onSuccess, onError }: {
  expectedFormats: IScannerProps['formats']
  onSuccess: (result: IDetectedBarcode) => void,
  onError: (error: string) => void
}) => {
  return <div style={{ width: '80%', height: '80%' }}>
    <Scanner
      formats={expectedFormats}
      components={{ audio: false }}
      onScan={detectedCodes => {
        if (detectedCodes.length > 1) {
          onError('Multiple barcodes detected. Please scan again with only one barcode visible.')
        } else if (detectedCodes.length === 1) {
          onSuccess(detectedCodes[0])
        } else {
          onError('No barcode detected. Please try again.')
        }
      }}/>
  </div>
}
