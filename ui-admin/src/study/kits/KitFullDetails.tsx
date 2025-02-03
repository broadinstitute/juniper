import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import React from 'react'
import { InfoCard, InfoCardHeader } from 'components/InfoCard'
import { useParams } from 'react-router-dom'
import { Enrollee, instantToDefaultString } from '@juniper/ui-core'
import { NavBreadcrumb } from '../../navbar/AdminNavbar'
import { useAdminUserContext } from '../../providers/AdminUserProvider'
import { KitRequestAddress } from '../participants/KitRequests'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFedex } from '@fortawesome/free-brands-svg-icons'

export function KitFullDetails({ enrollee, studyEnvContext }: {
    enrollee: Enrollee, studyEnvContext: StudyEnvContextT
}) {
  const { users } = useAdminUserContext()
  const { kitRequestId } = useParams<{ kitRequestId: string }>()
  const kitRequest = enrollee.kitRequests.find(kitRequest => kitRequest.id === kitRequestId)

  return <>
    <NavBreadcrumb value={studyEnvContext.currentEnvPath}>
      kit
    </NavBreadcrumb>
    <InfoCard>
      <InfoCardHeader>
        <div className="d-flex justify-content-between align-items-center w-100">
          <div className="fw-bold lead my-1">Kit Requests</div>
        </div>
      </InfoCardHeader>
      {!kitRequest && <div>Kit request not found</div>}
      {kitRequest && <div className={'container pt-3'}>
        <div className={'row'}>
          <div className="col-6 border rounded p-3">
            <div className="fw-bold">Sent To</div>
            <KitRequestAddress sentToAddressJson={kitRequest.sentToAddress}/>
            <FontAwesomeIcon className={'fa-xl'} icon={faFedex}/> {kitRequest.trackingNumber}
          </div>
          <div className="col-6 border rounded p-3">
            <div className="fw-bold">Latest Status</div>
            {kitStatusBadge(kitRequest.status)}
          </div>
        </div>
        <div className={'pt-3'}>
          <span className="fw-bold">Timeline</span>
          <div className="py-3 border rounded-3">
            {timelineEvent(
                `Requested by ${users.find(user => user.id === kitRequest.creatingAdminUserId)?.username}`,
                kitRequest.createdAt
            )}
            {timelineEvent(
                `Queued for shipment`
            )}
            {timelineEvent(
                `Shipped to participant ${kitRequest.trackingNumber}`
            )}
            {timelineEvent(
                `Returned by participant ${kitRequest.returnTrackingNumber}`
            )}
            <div className={'text-center py-3 fst-italic text-muted'}>
              Status updates will appear here as they occur
            </div>
          </div>
        </div>
      </div>}
    </InfoCard>
  </>
}

const kitStatusBadge = (status: string) => {
  return <div className={'badge rounded-pill bg-primary'}>{status}</div>
}

const timelineEvent = (eventText: string, timestamp?: number) => {
  return <div className={'d-flex my-2'}>
    { timestamp ?
      <div className="fw-bold text-center" style={{ width: '40%' }}>{instantToDefaultString(timestamp)}</div> :
      <div className="text-muted fw-bold text-center" style={{ width: '40%' }}>|</div>
    }
    <div className="">{eventText}</div>
  </div>
}
