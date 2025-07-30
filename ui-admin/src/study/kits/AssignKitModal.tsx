import {
  paramsFromContext,
  StudyEnvContextT
} from '../StudyEnvironmentRouter'
import React, { useState } from 'react'
import { useKitTypeSelect } from '../participants/RequestKitModal'
import { doApiLoad } from 'api/api-utils'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import { Modal } from 'react-bootstrap'
import LoadingSpinner from 'util/LoadingSpinner'
import { Enrollee } from '@juniper/ui-core'
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
  returnTrackingNumber?: string,
}
/** Renders a modal for an admin to quickly scan & send one or more kit requests. */
export default function AssignKitModal({
  studyEnvContext, enrollee,
  onDismiss, onSubmit, queueIdx, queueLength
}: {
  studyEnvContext: StudyEnvContextT,
  onDismiss: () => void,
  enrollee: Enrollee,
  onSubmit: (anyKitWasCreated: boolean) => void,
  queueIdx?: number,
  queueLength?: number
}) {
  const [isLoading, setIsLoading] = useState(false)

  const [kitLabel, setKitLabel] = useState<string>()
  const [returnTrackingNumber, setReturnTrackingNumber] = useState<string>()


  const { kitType, KitSelect } = useKitTypeSelect(paramsFromContext(studyEnvContext))

  const assembledKitDto: ManualKitRequestCreationDto = {
    kitType,
    distributionMethod: 'MANUAL',
    kitLabel: kitLabel || '',
    skipAddressValidation: false,
    returnTrackingNumber
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
        {
          kitType: 'SALIVA',
          distributionMethod: 'MANUAL',
          kitLabel,
          returnTrackingNumber,
          skipAddressValidation: false
        }
      )
      Store.addNotification(successNotification('Kit successfully assigned'))
      onSubmit(true)
    }, {
      setError: error => {
        if (error) {
          Store.addNotification(failureNotification(`Failed to assign kit: ${error}`))
        }
      },
      setIsLoading
    })
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
          </InfoCardBody>
        </InfoCard>

        <InfoCard>
          <InfoCardHeader>
            <InfoCardTitle title={'Kit Details'}/>
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
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
        <button className='btn btn-primary' onClick={assignKit} disabled={!isKitComplete || isLoading}>
          Assign kit
        </button>
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
