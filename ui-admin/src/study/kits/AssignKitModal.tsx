import {
  paramsFromContext,
  StudyEnvContextT
} from '../StudyEnvironmentRouter'
import React, { useState } from 'react'
import { useKitTypeSelect } from '../participants/RequestKitModal'
import {
  doApiLoad,
  useLoadingEffect
} from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import { Modal } from 'react-bootstrap'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  Enrollee,
  instantToDateString,
  KitRequest
} from '@juniper/ui-core'
import {
  isEmpty,
  isNil,
  startCase
} from 'lodash'
import { BarcodeScanner } from 'study/kits/kitcollection/BarcodeScanner'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCamera } from '@fortawesome/free-solid-svg-icons'
import { Checkbox } from 'components/forms/Checkbox'
import { Textarea } from 'components/forms/Textarea'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardRow,
  InfoCardTitle,
  InfoCardValue
} from 'components/InfoCard'

type ManualKitRequestCreationDto = {
  distributionMethod: 'MANUAL',
  kitType: string,
  kitLabel: string,
  skipAddressValidation: boolean,
  trackingNumber?: string,
  returnTrackingNumber?: string,
}
/** Renders a modal for an admin to quickly scan & send one or more kit requests. */
export default function AssignKitModal({
  studyEnvContext, enrollee,
  onDismiss, onSubmit, queueIdx, queueLength, skip
}: {
  studyEnvContext: StudyEnvContextT,
  onDismiss: () => void,
  enrollee: Enrollee,
  onSubmit: (anyKitWasCreated: boolean) => void,
  queueIdx?: number,
  queueLength?: number,
  skip?: () => void
}) {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [kitLabel, setKitLabel] = useState<string>()
  const [trackingNumber, setTrackingNumber] = useState<string>()
  const [returnTrackingNumber, setReturnTrackingNumber] = useState<string>()

  const [enrolleeKits, setEnrolleeKits] = useState<KitRequest[]>([])

  const {
    isLoading: isLoadingEnrolleeKits
  } = useLoadingEffect(async () => {
    const kits = await Api.fetchEnrolleeKitRequests(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      enrollee.shortcode)

    setEnrolleeKits(kits)
  }, [studyEnvContext, enrollee.shortcode], 'Loading enrollee kits')

  const isLoading = isLoadingEnrolleeKits || isSubmitting


  const { kitType, KitSelect } = useKitTypeSelect(paramsFromContext(studyEnvContext))

  const assembledKitDto: ManualKitRequestCreationDto = {
    kitType,
    distributionMethod: 'MANUAL',
    kitLabel: kitLabel || '',
    skipAddressValidation: false,
    returnTrackingNumber,
    trackingNumber
  }
  // only required field is kitLabel
  const isKitComplete = !isEmpty(assembledKitDto.kitLabel)


  const assignKit = async () => {
    if (!enrollee || !kitLabel) {
      return
    }
    doApiLoad(async () => {
      await Api.createKitRequest(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        enrollee.shortcode,
        assembledKitDto
      )
      Store.addNotification(successNotification('Kit successfully assigned'))
      onSubmit(true)
      setKitLabel(undefined)
      setReturnTrackingNumber(undefined)
      setTrackingNumber(undefined)
    }, {
      setError: error => {
        if (error) {
          Store.addNotification(failureNotification(`Failed to assign kit: ${error}`))
        }
      },
      setIsLoading: setIsSubmitting
    })
  }

  const naturalCase = (str: string) => {
    return startCase(str.toLowerCase())
  }


  return <Modal show={true} onHide={onDismiss} size="lg">
    <Modal.Header closeButton>
      <div className="d-flex align-items-center justify-content-between w-100 pe-2">
        <Modal.Title>Assign Kit</Modal.Title>
        {!isNil(queueIdx) && !isNil(queueLength) &&
            <div className="ms-2 text-muted">
              {queueIdx + 1} of {queueLength}
            </div>}
      </div>
    </Modal.Header>
    <Modal.Body>
      <h4>Profile</h4>
      {isLoading ? <LoadingSpinner/> : <>
        <InfoCard>
          <InfoCardHeader>
            <InfoCardTitle title={'Enrollee'}/>
          </InfoCardHeader>
          <InfoCardBody>
            <InfoCardValue title={'Shortcode'} values={[enrollee.shortcode]} condensed/>
            <InfoCardValue title={'Name'}
              values={[`${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`]}
              condensed/>
            <InfoCardValue title={'Sex At Birth'} values={[enrollee.profile?.sexAtBirth || '']} condensed/>

            {enrolleeKits.map(kit => {
              return <>
                <InfoCardRow
                  title={`${naturalCase(kit.kitType.name)} Kit (${instantToDateString(kit.createdAt)})`}
                  condensed>
                  <p className='m-0'>
                    Status: <span className='fst-italic'>{naturalCase(kit.status)}</span>
                  </p>
                  <p className='m-0'>
                    Label: <span className='fst-italic'>{kit.kitLabel || 'N/A'}</span>
                  </p>
                  <p className='m-0'>
                    Return Tracking: <span className='fst-italic'>{kit.returnTrackingNumber || 'N/A'}</span>
                  </p>
                  <p className='m-0'>
                    Sent: <span className='fst-italic'>{kit.sentAt ? instantToDateString(kit.sentAt) : 'N/A'}</span>
                  </p>
                </InfoCardRow>
              </>
            })}
          </InfoCardBody>
        </InfoCard>

        <InfoCard>
          <InfoCardHeader>
            <InfoCardTitle title={'New Kit'}/>
          </InfoCardHeader>
          <InfoCardBody>
            <InfoCardRow title={'Kit Type'}>
              {KitSelect}
            </InfoCardRow>
            <InfoCardRow title={'Kit Label'}>
              <LabelScanner
                field={'kitLabel'}
                value={kitLabel || ''}
                setValue={setKitLabel}/>
            </InfoCardRow>
            <InfoCardRow title={'Tracking Number (optional)'}>
              <LabelScanner
                field={'trackingNumber'}
                value={trackingNumber || ''}
                setValue={setTrackingNumber}/>
            </InfoCardRow>
            <InfoCardRow title={'Return Tracking (optional)'}>
              <LabelScanner
                field={'returnTracking'}
                value={returnTrackingNumber || ''}
                setValue={setReturnTrackingNumber}/>
            </InfoCardRow>
          </InfoCardBody>
        </InfoCard>
      </>}
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <div className="d-flex justify-content-between w-100">
          <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
          <div>
            {skip &&
                <button className='btn btn-secondary me-2' onClick={skip}>
                    Skip
                </button>}
            <button className='btn btn-primary' onClick={assignKit} disabled={!isKitComplete || isLoading}>
              Assign kit
            </button>
          </div>
        </div>

      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}


const LabelScanner = ({
  field,
  value,
  setValue
}: {
  field: string,
  value: string,
  setValue: (value: string) => void
}) => {
  const title = startCase(field).toLowerCase()

  const [showScanner, setShowScanner] = useState(false)
  const [enableOverride, setEnableOverride] = useState(false)
  const [error, setError] = useState<string>()

  return <>
    {showScanner &&
        <BarcodeScanner
          expectedFormats={['code_128']}
          onError={error => setError(error)}
          onSuccess={result => {
            setValue(result.rawValue)
            setShowScanner(false)
          }}/>
    }
    <Button className="my-2" variant={'primary'}
      onClick={() => setShowScanner(!showScanner)}>
      <FontAwesomeIcon icon={faCamera} className={'pe-2'}/>Click to scan {title}
    </Button>
    <Checkbox
      label={`Enable manual ${title} override`}
      checked={enableOverride}
      onChange={e => {
        setEnableOverride(e)
      }}/>
    <Textarea
      className="my-2"
      rows={2}
      disabled={!enableOverride}
      placeholder={`Scan ${title}`}
      value={value}
      onChange={e => setValue(e)}>
    </Textarea>
    {error &&
        <div className="text-danger">{error}</div>
    }
  </>
}
