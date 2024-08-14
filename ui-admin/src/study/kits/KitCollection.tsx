import React, { ReactNode, useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { dateToDefaultString, Enrollee } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleCheck, faCircleDot, faCircleExclamation } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { useUser } from 'user/UserProvider'
import { IDetectedBarcode, IScannerProps, Scanner } from '@yudiel/react-qr-scanner'


export const KitCollection = ({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) => {
  //Step 1 state
  const [showEnrolleeCodeScanner, setShowEnrolleeCodeScanner] = useState(false)
  const [enrollee, setEnrollee] = useState<Enrollee>()
  const [enrolleeCodeError, setEnrolleeCodeError] = useState<string>()

  //Step 2 state
  const [isEnrolleeIdentityConfirmed, setIsEnrolleeIdentityConfirmed] = useState(false)

  //Step 3 state
  const [showKitScanner, setShowKitScanner] = useState(false)
  const [kitId, setKitId] = useState<string>()
  const [kitCodeError, setKitCodeError] = useState<string>()

  const { user } = useUser()

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
        setIsEnrolleeIdentityConfirmed(false)
      } else {
        setEnrolleeCodeError('Enrollee has not consented to the study')
        setEnrollee(undefined)
      }
    }, {
      setIsError: error => {
        if (error) {
          setEnrolleeCodeError('Error loading enrollee. Did you scan the correct QR code?')
          setEnrollee(undefined)
        }
      }
    })
  }

  //While under development, we will only allow superusers to access this page
  return user?.superuser ?
    <div className='m-2' style={{ maxWidth: '450px' }}>
      {renderPageHeader('Collect a kit')}

      <div className={'text-muted mb-1'}>
        To collect an in-person kit, follow the steps below. Please ensure
        that all information is correct before submitting.
      </div>

      <KitCollectionStep
        stepNumber={1}
        description={<>
          Click to open the camera and scan the enrollee&apos;s barcode
          <InfoPopup content={'The enrollee can find their unique QR code by going to their profile'}/>
        </>}
        status={enrolleeCodeError ? 'ERROR' : enrollee ? 'COMPLETE' : 'INCOMPLETE'}
      >
        <Button className="mb-2" variant={'primary'} onClick={() => {
          setEnrollee(undefined)
          setShowEnrolleeCodeScanner(!showEnrolleeCodeScanner)
          setIsEnrolleeIdentityConfirmed(false)
          setKitId(undefined)
        }}>
        Click to scan enrollee code
        </Button>
        { showEnrolleeCodeScanner &&
        <BarcodeScanner
          expectedFormats={['qr_code']}
          onError={error => {
            setEnrolleeCodeError(error)
            setShowEnrolleeCodeScanner(false)
            setEnrollee(undefined)
          }}
          onSuccess={result => {
            loadEnrollee(result.rawValue)
            setEnrolleeCodeError(undefined)
            setShowEnrolleeCodeScanner(false)
          }}/>
        }
        { enrolleeCodeError &&
          <div className="text-danger">{enrolleeCodeError}</div>
        }
      </KitCollectionStep>

      <KitCollectionStep
        stepNumber={2}
        status={isEnrolleeIdentityConfirmed && enrollee ? 'COMPLETE' : 'INCOMPLETE'}
        description={<>Confirm the enrollee&apos;s identity</>}
      >
        <TextInput
          label={'Enrollee Name'}
          className="mb-2" disabled={true}
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
          disabled={!enrollee} onClick={() => setIsEnrolleeIdentityConfirmed(true)}>
          Mark as confirmed
        </Button>
      </KitCollectionStep>

      <KitCollectionStep
        stepNumber={3}
        description={'Click to open the camera and scan the kit barcode'}
        status={kitId ? 'COMPLETE' : 'INCOMPLETE'}
      >
        <Button className="mb-2" variant={'primary'} disabled={!isEnrolleeIdentityConfirmed}
          tooltip={!isEnrolleeIdentityConfirmed ? 'You must complete the prior steps first' : ''}
          onClick={() => setShowKitScanner(!showKitScanner)}>
        Click to scan kit barcode
        </Button>
        { showKitScanner &&
          <BarcodeScanner
            expectedFormats={['code_128']}
            onError={error => setKitCodeError(error)}
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
        { kitCodeError &&
          <div className="text-danger">{kitCodeError}</div>
        }
      </KitCollectionStep>
      <div className="d-flex justify-content-end">
        <Button disabled={!(kitId && enrollee)} variant={'primary'}
          onClick={() => {
            setEnrollee(undefined)
            setIsEnrolleeIdentityConfirmed(false)
            setKitId(undefined)
          }}>
        Submit kit
        </Button>
      </div>
    </div> :
    <div className="m-2">
      <div className="alert alert-danger" role="alert">
        You do not have permission to access this page.
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
