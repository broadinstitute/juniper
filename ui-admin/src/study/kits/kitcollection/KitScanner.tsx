import React, { ReactNode, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { dateToDefaultString, Enrollee } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { useUser } from 'user/UserProvider'
import { BarcodeScanner } from './BarcodeScanner'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCircleCheck,
  faCircleDot,
  faCircleExclamation,
  faCircleQuestion,
  faSearch
} from '@fortawesome/free-solid-svg-icons'
import { Checkbox } from 'components/forms/Checkbox'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

const kitScanModeOptions = [
  { value: 'ASSIGN', label: 'Assign a new kit' },
  { value: 'COLLECT', label: 'Collect a completed kit' }
]

export const KitScanner = ({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) => {
  const [selectedScanMode, setSelectedScanMode] = useState<{ value: string, label: string } | undefined>()
  const [showEnrolleeCodeScanner, setShowEnrolleeCodeScanner] = useState(false)
  const [showKitScanner, setShowKitScanner] = useState(false)
  const [enrollee, setEnrollee] = useState<Enrollee>()
  const [enrolleeCodeError, setEnrolleeCodeError] = useState<string>()
  const [isEnrolleeIdentityConfirmed, setIsEnrolleeIdentityConfirmed] = useState(false)
  const [kitBarcode, setKitBarcode] = useState<string | undefined>()
  const [kitCodeError, setKitCodeError] = useState<string>()

  const [enableManualOverride, setEnableManualOverride] = useState(false)
  const [enrolleeShortcodeOverride, setEnrolleeShortcodeOverride] = useState<string>()

  const { user } = useUser()

  const assignKit = async () => {
    if (!enrollee || !kitBarcode) { return }
    doApiLoad(async () => {
      await Api.createKitRequest(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        {
          kitType: 'SALIVA',
          distributionMethod: 'ASSIGNED',
          kitBarcode,
          skipAddressValidation: false
        }
      )
      Store.addNotification(successNotification('Kit successfully assigned'))
    }, {
      setIsError: error => {
        if (error) {
          setEnrolleeCodeError('Error assigning kit')
        }
      }
    })
  }

  const collectKit = async () => {
    if (!enrollee || !kitBarcode) { return }
    doApiLoad(async () => {
      await Api.collectKit(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        {
          kitBarcode
        }
      )
      Store.addNotification(successNotification('Kit successfully collected'))
    }, {
      setIsError: error => {
        if (error) {
          setEnrolleeCodeError('Error collecting kit')
        }
      }
    })
  }

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

  //TODO: JN-1295, while under development, we'll only allow superusers to access this page
  if (!user?.superuser) {
    return <div className="m-2">
      <div className="alert alert-danger" role="alert">
          You do not have permission to access this page.
      </div>
    </div>
  }

  return <div className='m-2' style={{ maxWidth: '450px' }}>
    {renderPageHeader('Scan a kit')}

    <div className={'text-muted mb-1'}>
      To assign or collect an in-person kit, follow the steps below. Please ensure
      that all information is correct before submitting.
    </div>

    { user.superuser && <Checkbox
      label={'Enable manual override mode'}
      checked={enableManualOverride} onChange={e => {
        setEnableManualOverride(e)
      }}/>
    }

    <KitCollectionStepWrapper
      title={'Step 1'}
      status={selectedScanMode ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">
        Are you assigning a new kit to a participant or collecting a completed kit?
      </div>
      <Select
        value={selectedScanMode}
        options={kitScanModeOptions}
        onChange={selectedOption => {
          setSelectedScanMode(kitScanModeOptions.find(option => option.value === selectedOption?.value))
          setEnrollee(undefined)
          setIsEnrolleeIdentityConfirmed(false)
          setKitBarcode(undefined)
        }}
      />
    </KitCollectionStepWrapper>

    <KitCollectionStepWrapper
      title={'Step 2'}
      status={enrolleeCodeError ? 'ERROR' : enrollee ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">
        Click to open the camera and scan the enrollee&apos;s QR code
        <InfoPopup content={'The enrollee can find their unique QR code by going to their profile'}/>
      </div>
      <Button className="mb-2" disabled={!selectedScanMode} variant={'primary'} onClick={() => {
        setEnrollee(undefined)
        setShowEnrolleeCodeScanner(!showEnrolleeCodeScanner)
        setIsEnrolleeIdentityConfirmed(false)
        setKitBarcode(undefined)
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
      { enableManualOverride && <div className="d-flex align-items-center">
        <TextInput
          disabled={false}
          value={enrolleeShortcodeOverride}
          onChange={e => setEnrolleeShortcodeOverride(e)}>
        </TextInput>
        <Button className="ms-2" variant={'primary'} onClick={() => {
          setEnrolleeCodeError(undefined)
          loadEnrollee(enrolleeShortcodeOverride!)
        }}>
          <FontAwesomeIcon icon={faSearch}/>
        </Button>
      </div> }
      { enrolleeCodeError &&
        <div className="text-danger">{enrolleeCodeError}</div>
      }
    </KitCollectionStepWrapper>

    <KitCollectionStepWrapper
      title={'Step 3'}
      status={isEnrolleeIdentityConfirmed && enrollee ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">Confirm the enrollee&apos;s identity</div>
      <TextInput
        label={'Enrollee Name'}
        className="mb-2" disabled={true}
        value={enrollee ? `${enrollee.profile.givenName} ${enrollee.profile.familyName}` : ''}>
      </TextInput>
      <TextInput
        label={'Date of Birth'}
        className="mb-3" disabled={true}
        value={enrollee ? dateToDefaultString(enrollee.profile.birthDate) : ''}>
      </TextInput>
      <Button className="mb-2" variant={'primary'}
        disabled={!enrollee} onClick={() => setIsEnrolleeIdentityConfirmed(true)}>
            Mark as confirmed
      </Button>
    </KitCollectionStepWrapper>

    <KitCollectionStepWrapper
      title={'Step 4'}
      status={kitBarcode ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">Click to open the camera and scan the kit barcode</div>
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
            setKitBarcode(result.rawValue)
            setShowKitScanner(false)
          }}/>
      }
      { enableManualOverride && <TextInput
        className="my-2"
        disabled={false}
        value={kitBarcode}
        onChange={e => setKitBarcode(e)}>
      </TextInput> }
      { kitCodeError &&
            <div className="text-danger">{kitCodeError}</div>
      }
    </KitCollectionStepWrapper>
    <div className="d-flex justify-content-end">
      <Button disabled={!(kitBarcode && enrollee && selectedScanMode)} variant={'primary'}
        onClick={async () => {
          setEnrollee(undefined)
          setIsEnrolleeIdentityConfirmed(false)
          setKitBarcode(undefined)
          setEnrolleeCodeError(undefined)
          setEnrolleeShortcodeOverride(undefined)
          setSelectedScanMode(undefined)
          if (selectedScanMode?.value === 'ASSIGN') {
            await assignKit()
          } if (selectedScanMode?.value === 'COLLECT') {
            await collectKit()
          }
        }}>
          Submit
      </Button>
    </div>
  </div>
}

const KitCollectionStepWrapper = ({ title, status, children }: {
  title: string, status: StepStatus, children: ReactNode, disabled?: boolean
}) => {
  return <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
    <h2 className="d-flex align-items-center mb-3">
      { stepStatusToIcon(status) } {title}
    </h2>
    <div className='mb-2'>{children}</div>
  </div>
}

export type StepStatus = 'COMPLETE' | 'INCOMPLETE' | 'ERROR'

export const stepStatusToIcon = (status: StepStatus) => {
  switch (status) {
    case 'COMPLETE':
      return <FontAwesomeIcon className="text-success me-1" icon={faCircleCheck}/>
    case 'INCOMPLETE':
      return <FontAwesomeIcon className="text-muted me-1" icon={faCircleDot}/>
    case 'ERROR':
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleExclamation}/>
    default:
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleQuestion}/>
  }
}
