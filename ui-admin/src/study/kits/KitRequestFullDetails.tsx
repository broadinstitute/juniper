import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import React from 'react'
import { InfoCard, InfoCardHeader } from 'components/InfoCard'
import { useParams } from 'react-router-dom'
import { Enrollee, instantToDefaultString, KitRequest, KitRequestStatus, SUPPORT_EMAIL_ADDRESS } from '@juniper/ui-core'
import { NavBreadcrumb } from '../../navbar/AdminNavbar'
import { useAdminUserContext } from '../../providers/AdminUserProvider'
import { KitRequestAddress } from '../participants/KitRequests'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFedex, faUsps } from '@fortawesome/free-brands-svg-icons'
import { AdminUser } from '../../api/adminUser'
import { faCircleCheck, faQuestion, faSpinner, faTruckFast } from '@fortawesome/free-solid-svg-icons'
import { faCircleXmark } from '@fortawesome/free-regular-svg-icons'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'

export function KitRequestFullDetails({ enrollee, studyEnvContext }: {
    enrollee: Enrollee, studyEnvContext: StudyEnvContextT
}) {
  const { users } = useAdminUserContext()
  const { kitRequestId } = useParams<{ kitRequestId: string }>()
  const kitRequest = enrollee.kitRequests.find(kitRequest => kitRequest.id === kitRequestId)

  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      kit
    </NavBreadcrumb>
    {kitRequest && <div>
      <div className={'d-flex gap-3'}>
        {quickLookInfo(kitRequest)}
        {shippingInformation(kitRequest, users)}
      </div>

      <InfoCard>
        <InfoCardHeader>
          <div className="d-flex justify-content-between align-items-center w-100">
            <div className="fw-bold lead my-1">Timeline</div>
          </div>
        </InfoCardHeader>
        <div className={'my-3'}>
          {timelineEvent(
                  `Requested by ${users.find(user => user.id === kitRequest.creatingAdminUserId)?.username}`,
                  kitRequest.createdAt
          )}
          {timelineEvent(
                  `Queued for shipment`
          )}
          {timelineEvent(
            <>Shipped to participant {uspsTrackingLink(kitRequest.trackingNumber)}</>
          )}
          {timelineEvent(
            <>Returned by participant {fedexTrackingLink(kitRequest.returnTrackingNumber)}</>
          )}
          <div className={'text-center pt-3 fst-italic text-muted'}>
                Status updates will appear here as they occur
          </div>
        </div>
      </InfoCard>
    </div>}
  </>
}

const timelineEvent = (timelineEvent: React.ReactNode, timestamp?: number) => {
  return <div className={'d-flex my-2'}>
    {timestamp ?
      <div className="fw-bold text-center" style={{ width: '40%' }}>{instantToDefaultString(timestamp)}</div> :
      <div className="text-muted fw-bold text-center" style={{ width: '40%' }}>|</div>
    }
    <div className="">{timelineEvent}</div>
  </div>
}

const shippingInformation = (kitRequest: KitRequest, users: AdminUser[]) => {
  return <InfoCard>
    <InfoCardHeader>
      <div className="d-flex justify-content-between align-items-center w-100">
        <div className="fw-bold lead my-1">Shipping Address</div>
      </div>
    </InfoCardHeader>
    <div className={'m-3'}>
      {kitRequest.distributionMethod === 'MAILED' ?
        <>
          <KitRequestAddress sentToAddressJson={kitRequest.sentToAddress}/>
        </> : `This kit was distributed to the participant in person by ${users.find(
          user => user.id === kitRequest.creatingAdminUserId
        )?.username}.`}
    </div>
  </InfoCard>
}

const quickLookInfo = (kitRequest: KitRequest) => {
  return <InfoCard>
    <InfoCardHeader>
      <div className="d-flex justify-content-between align-items-center w-100">
        <div className="fw-bold lead my-1">Latest Status</div>
      </div>
    </InfoCardHeader>
    <div className={'m-3'}>
      {KitStatusBadge({ status: kitRequest.status })}
    </div>
  </InfoCard>
}

const StatusBadge = ({ icon, iconClass, message }: { icon: IconDefinition, iconClass?: string, message: string }) => {
  return (
    <div className="d-flex align-items-center">
      <FontAwesomeIcon className={`fa-4x ${iconClass}`} icon={icon} />
      <div className={'ms-5'}>{message}</div>
    </div>
  )
}

const KitStatusBadge = ({ status } : { status: KitRequestStatus}) => {
  switch (status) {
    case 'CREATED':
      return <StatusBadge
        icon={faSpinner}
        message="This kit request has been created in our system and is being processed."
      />
    case 'QUEUED':
      return <StatusBadge
        icon={faSpinner}
        message="This kit is being queued for shipment."
      />
    case 'SENT':
      return <StatusBadge
        icon={faTruckFast}
        message="This kit has been shipped to the participant."
      />
    case 'RECEIVED':
      return <StatusBadge
        icon={faCircleCheck}
        iconClass="text-success"
        message="This kit has been returned by the participant."
      />
    case 'DEACTIVATED':
      return <StatusBadge
        icon={faCircleXmark}
        iconClass="text-danger"
        message="This kit has been deactivated."
      />
    default:
      return (
        <div className="d-flex align-items-center">
          <FontAwesomeIcon className="fa-4x" icon={faQuestion} />
          <div className={'ms-4'}>
              This kit is in an unknown state.
            Please contact <a href={`mailto:${SUPPORT_EMAIL_ADDRESS}`}>{SUPPORT_EMAIL_ADDRESS}</a>
            for additional information.
          </div>
        </div>
      )
  }
}

const uspsTrackingLink = (trackingNumber?: string) => {
  return trackingNumber ?
    <a target="_blank" href={`https://tools.usps.com/go/TrackConfirmAction_input?strOrigTrackNum=${trackingNumber}`}>
      <FontAwesomeIcon className="ms-1" icon={faUsps}/> {trackingNumber}
    </a>: null
}

const fedexTrackingLink = (trackingNumber?: string) => {
  return trackingNumber ?
    <a target="_blank" href={`https://www.fedex.com/apps/fedextrack/?action=track&trackingnumber=${trackingNumber}`}>
      <FontAwesomeIcon className="fa-xl ms-1" icon={faFedex}/> {trackingNumber}
    </a> : null
}

const advancedInformation = (kitRequest: KitRequest) => {
  return <InfoCard>
    <InfoCardHeader>
      <div className="d-flex justify-content-between align-items-center w-100">
        <div className="fw-bold lead my-1">Advanced Information</div>
      </div>
    </InfoCardHeader>
    <div className={'m-3'}>
      <div className="d-flex">Skip address validation: {kitRequest.skipAddressValidation ? 'yes' : 'no'}</div>
      {kitRequest.details || ''}
    </div>
  </InfoCard>
}
