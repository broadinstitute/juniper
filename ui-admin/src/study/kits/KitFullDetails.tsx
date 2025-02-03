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
          <div className="fw-bold lead my-1">Kit Request</div>
        </div>
      </InfoCardHeader>
      {!kitRequest && <div>Details for this kit request could not be displayed. Please contact support.</div>}
      {kitRequest && <div className={'d-flex pt-3'}>
        <div className={'d-flex flex-row'}>
          <div className="border rounded p-3 ms-2">
            <div className="fw-bold">Sent To</div>
            <KitRequestAddress sentToAddressJson={kitRequest.sentToAddress}/>
            <FontAwesomeIcon className={'fa-xl'} icon={faFedex}/> {kitRequest.trackingNumber}
          </div>
          <div className="border rounded p-3">
            <div className="fw-bold">Latest Status</div>
            {kitStatusBadge(kitRequest.status)}
          </div>
        </div>
        <div className={'d-flex flex-row pt-3'}>
          <div className="py-3 border rounded-3 mb-3">
            <h5 className="fw-bold ps-3 border-bottom pb-3">Timeline</h5>
            <div className={'mt-3'}>
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
