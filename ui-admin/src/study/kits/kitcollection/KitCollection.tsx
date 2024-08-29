import React, { useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Button } from 'components/forms/Button'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { dateToDefaultString, Enrollee, KitCollectionStepWrapper } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import { renderPageHeader } from 'util/pageUtils'
import InfoPopup from 'components/forms/InfoPopup'
import { useUser } from 'user/UserProvider'
import { BarcodeScanner } from './BarcodeScanner'

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

  //TODO: JN-1295, while under development, we'll only allow superusers to access this page
  if (!user?.superuser) {
    return <div className="m-2">
      <div className="alert alert-danger" role="alert">
          You do not have permission to access this page.
      </div>
    </div>
  }

  return <div className='m-2' style={{ maxWidth: '450px' }}>
    {renderPageHeader('Collect a kit')}

    <div className={'text-muted mb-1'}>
      To collect an in-person kit, follow the steps below. Please ensure
      that all information is correct before submitting.
    </div>

    <KitCollectionStepWrapper
      title={'Step 1'}
      status={enrolleeCodeError ? 'ERROR' : enrollee ? 'COMPLETE' : 'INCOMPLETE'}
    >
      <div className="mb-3">
        Click to open the camera and scan the enrollee&apos;s barcode
        <InfoPopup content={'The enrollee can find their unique QR code by going to their profile'}/>
      </div>
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
    </KitCollectionStepWrapper>

    <KitCollectionStepWrapper
      title={'Step 2'}
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
      title={'Step 3'}
      status={kitId ? 'COMPLETE' : 'INCOMPLETE'}
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
    </KitCollectionStepWrapper>
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
  </div>
}
