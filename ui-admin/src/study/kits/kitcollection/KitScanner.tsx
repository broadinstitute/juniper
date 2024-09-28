import React, { ReactNode, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { dateToDefaultString, Enrollee } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { BarcodeScanner } from './BarcodeScanner'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCamera,
  faCircle,
  faCircleCheck,
  faCircleExclamation,
  faCircleQuestion,
  faSearch
} from '@fortawesome/free-solid-svg-icons'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { Checkbox } from 'components/forms/Checkbox'

const kitScanModeOptions = [
  { value: 'ASSIGN', label: 'Assign a new kit' },
  { value: 'COLLECT', label: 'Collect a completed kit' }
]

export const KitScanner = ({ studyEnvContext }: { studyEnvContext: StudyEnvContextT }) => {
  const [selectedScanMode, setSelectedScanMode] = useState<{ value: string, label: string } | undefined>()
  const [showEnrolleeCodeScanner, setShowEnrolleeCodeScanner] = useState(false)
  const [showKitScanner, setShowKitScanner] = useState(false)
  const [showReturnTrackingNumberScanner, setShowReturnTrackingNumberScanner] = useState(false)
  const [enrollee, setEnrollee] = useState<Enrollee>()
  const [enrolleeCodeError, setEnrolleeCodeError] = useState<string>()
  const [isEnrolleeIdentityConfirmed, setIsEnrolleeIdentityConfirmed] = useState(false)
  const [kitLabel, setKitLabel] = useState<string>()
  const [kitCodeError, setKitCodeError] = useState<string>()
  const [returnTrackingNumber, setReturnTrackingNumber] = useState<string>()
  const [returnTrackingNumberError, setReturnTrackingNumberError] = useState<string>()

  const [enableManualShortcodeOverride, setEnableManualShortcodeOverride] = useState(false)
  const [enableManualKitLabelOverride, setEnableManualKitLabelOverride] = useState(false)
  const [enableManualReturnLabelOverride, setEnableManualReturnLabelOverride] = useState(false)
  const [enrolleeShortcodeOverride, setEnrolleeShortcodeOverride] = useState<string>()

  const isSubmitDisabled = () => {
    if (!kitLabel || !enrollee || !selectedScanMode) {
      return true
    }
    return selectedScanMode.value === 'COLLECT' && !returnTrackingNumber
  }

  const assignKit = async () => {
    if (!enrollee || !kitLabel) { return }
    doApiLoad(async () => {
      await Api.createKitRequest(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        {
          kitType: 'SALIVA',
          distributionMethod: 'IN_PERSON',
          kitLabel,
          skipAddressValidation: false
        }
      )
      Store.addNotification(successNotification('Kit successfully assigned'))
    }, {
      setIsError: error => {
        if (error) {
          Store.addNotification(failureNotification('Error assigning kit'))
        }
      }
    })
  }

  const collectKit = async () => {
    if (!enrollee || !kitLabel || !returnTrackingNumber) {
      Store.addNotification(failureNotification('Please complete all steps before submitting'))
      return
    }
    doApiLoad(async () => {
      await Api.collectKit(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        {
          kitLabel,
          returnTrackingNumber
        }
      )
      Store.addNotification(successNotification('Kit successfully collected'))
    }, {
      setIsError: error => {
        if (error) {
          Store.addNotification(failureNotification('Error collecting kit'))
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

  return <div className='m-2' style={{ maxWidth: '450px' }}>
    {renderPageHeader('Scan a kit')}

    <div className={'text-muted mb-2'}>
      To assign or collect an in-person kit, follow the steps below. Please ensure
      that all information is correct before submitting.
    </div>

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
          setKitLabel(undefined)
        }}
      />
    </KitCollectionStepWrapper>

    <KitCollectionStepWrapper
      title={'Step 2'}
      status={enrolleeCodeError ? 'ERROR' : enrollee ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">
        Click the button below to open the camera and scan the enrollee&apos;s unique QR code.
        <InfoPopup content={
          <>The enrollee can find their unique QR code by going to the kits page on their profile.
          If you are unable to use your camera or scan the barcode for any reason, you may override
          manually and enter the participant code (shortcode) found directly under their QR code.</>}/>
      </div>
      <Button className="mb-2" disabled={!selectedScanMode} variant={'primary'} onClick={() => {
        setEnrollee(undefined)
        setShowEnrolleeCodeScanner(!showEnrolleeCodeScanner)
        setIsEnrolleeIdentityConfirmed(false)
        setKitLabel(undefined)
      }}>
        <FontAwesomeIcon icon={faCamera} className={'pe-2'}/>Click to scan enrollee code
      </Button>
      <Checkbox
        disabled={!selectedScanMode}
        label={'Enable manual shortcode override'}
        checked={enableManualShortcodeOverride} onChange={e => {
          setEnableManualShortcodeOverride(e)
        }}/>
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
      { enableManualShortcodeOverride && <div className="d-flex align-items-center">
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
      <div className="mb-3">Confirm the enrollee&apos;s identity using their full name and date of birth.</div>
      <TextInput
        label={'Full Name'}
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
      status={kitLabel ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">Click to open the camera and scan the label on the kit.</div>
      <Button className="mb-2" variant={'primary'} disabled={!isEnrolleeIdentityConfirmed}
        tooltip={!isEnrolleeIdentityConfirmed ? 'You must complete the prior steps first' : ''}
        onClick={() => setShowKitScanner(!showKitScanner)}>
        <FontAwesomeIcon icon={faCamera} className={'pe-2'}/>Click to scan kit label
      </Button>
      <Checkbox
        disabled={!isEnrolleeIdentityConfirmed}
        label={'Enable manual kit label override'}
        checked={enableManualKitLabelOverride} onChange={e => {
          setEnableManualKitLabelOverride(e)
        }}/>
      { showKitScanner &&
        <BarcodeScanner
          expectedFormats={['code_128']}
          onError={error => setKitCodeError(error)}
          onSuccess={result => {
            setKitLabel(result.rawValue)
            setShowKitScanner(false)
          }}/>
      }
      <TextInput
        className="my-2"
        disabled={!enableManualKitLabelOverride}
        placeholder={'Scan kit label'}
        value={kitLabel}
        onChange={e => setKitLabel(e)}>
      </TextInput>
      { kitCodeError &&
          <div className="text-danger">{kitCodeError}</div>
      }
    </KitCollectionStepWrapper>
    { selectedScanMode?.value === 'COLLECT' &&
        <KitCollectionStepWrapper
          title={'Step 5'}
          status={returnTrackingNumber ? 'COMPLETE' : 'INCOMPLETE'}
        >
          <div className="mb-3">Place the kit in the provided return packaging. Afterwards,
        click to open the camera and scan the return label on the return packaging.</div>
          <Button className="mb-2" variant={'primary'} disabled={!isEnrolleeIdentityConfirmed}
            tooltip={!kitLabel ? 'You must complete the prior steps first' : ''}
            onClick={() => setShowReturnTrackingNumberScanner(!showReturnTrackingNumberScanner)}>
            <FontAwesomeIcon icon={faCamera} className={'pe-2'}/>Click to scan kit return label
          </Button>
          <Checkbox
            disabled={!isEnrolleeIdentityConfirmed}
            label={'Enable manual return label override'}
            checked={enableManualReturnLabelOverride} onChange={e => {
              setEnableManualReturnLabelOverride(e)
            }}/>
          { showReturnTrackingNumberScanner &&
          <BarcodeScanner
            expectedFormats={['code_128']}
            onError={error => setReturnTrackingNumberError(error)}
            onSuccess={result => {
              setReturnTrackingNumber(result.rawValue)
              setShowReturnTrackingNumberScanner(false)
            }}/>
          }
          <TextInput
            className="my-2"
            disabled={!enableManualReturnLabelOverride}
            placeholder={'Scan return label'}
            value={returnTrackingNumber}
            onChange={e => setReturnTrackingNumber(e)}>
          </TextInput>
          { returnTrackingNumberError &&
          <div className="text-danger">{returnTrackingNumberError}</div>
          }
        </KitCollectionStepWrapper> }
    <div className="d-flex justify-content-end">
      <Button disabled={isSubmitDisabled()}
        variant={'primary'}
        onClick={async () => {
          setEnrollee(undefined)
          setIsEnrolleeIdentityConfirmed(false)
          setKitLabel(undefined)
          setEnrolleeCodeError(undefined)
          setEnrolleeShortcodeOverride(undefined)
          setEnableManualKitLabelOverride(false)
          setEnableManualShortcodeOverride(false)
          setEnableManualReturnLabelOverride(false)
          setSelectedScanMode(undefined)
          setReturnTrackingNumber(undefined)
          setReturnTrackingNumberError(undefined)
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
      return <FontAwesomeIcon className="text-muted me-1" icon={faCircle}/>
    case 'ERROR':
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleExclamation}/>
    default:
      return <FontAwesomeIcon className="text-danger me-1" icon={faCircleQuestion}/>
  }
}
