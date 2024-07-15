import React, { useState } from 'react'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import {
  basicTableLayout,
  renderEmptyMessage
} from 'util/tableUtils'
import RequestKitModal from './RequestKitModal'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  Enrollee,
  instantToDefaultString,
  KitRequest
} from '@juniper/ui-core'
import { useUser } from 'user/UserProvider'
import InfoPopup from 'components/forms/InfoPopup'
import KitStatusCell from './KitStatusCell'
import { Button } from 'components/forms/Button'
import {
  InfoCard,
  InfoCardHeader
} from 'components/InfoCard'

/** Component for rendering the address a kit was sent to based on JSON captured at the time of the kit request. */
function KitRequestAddress({ sentToAddressJson }: { sentToAddressJson: string }) {
  const address = JSON.parse(sentToAddressJson)
  return <div>
    <div>{address.firstName} {address.lastName}</div>
    <div>{address.street1}</div>
    {address.street2 && <div>{address.street2}</div>}
    <div>{address.city}, {address.state} {address.postalCode} {address.country}</div>
  </div>
}

const columns: ColumnDef<KitRequest, string>[] = [{
  header: 'Kit type',
  accessorKey: 'kitType.displayName'
}, {
  header: 'Status',
  accessorKey: 'status',
  cell: ({ row }) => <KitStatusCell kitRequest={row.original} infoPlacement='right'/>
}, {
  header: 'Created',
  accessorKey: 'createdAt',
  accessorFn: data => instantToDefaultString(data.createdAt)
}, {
  header: 'Address',
  cell: ({ row }) => <KitRequestAddress sentToAddressJson={row.original.sentToAddress}/>
}, {
  header: 'Details',
  accessorKey: 'details',
  cell: ({ row }) => <KitRequestDetails kitRequest={row.original}/>
}]

/**
 * Info popup for showing finer details of a kit request.
 */
export const KitRequestDetails = ({ kitRequest }: { kitRequest: KitRequest }) => {
  return <InfoPopup content={
    <div>
      <div className="d=flex">Skip address validation: {kitRequest.skipAddressValidation ? 'yes' : 'no'}</div>
      {kitRequest.details || ''}
    </div>
  } placement='left'/>
}

/** Shows a list of all kit requests for an enrollee. */
export default function KitRequests({ enrollee, studyEnvContext, onUpdate }:
                                      {
                                        enrollee: Enrollee,
                                        studyEnvContext: StudyEnvContextT,
                                        onUpdate: () => void
                                      }) {
  const { user } = useUser()
  const [showRequestKitModal, setShowRequestKitModal] = useState(false)

  const onSubmit = async () => {
    setShowRequestKitModal(false)
    onUpdate()
  }

  const table = useReactTable({
    data: enrollee.kitRequests || [],
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return <InfoCard>
    <InfoCardHeader>
      <div className="d-flex justify-content-between align-items-center w-100">
        <div className="fw-bold lead my-1">Kit Requests</div>
        {user?.superuser &&
            <Button onClick={() => setShowRequestKitModal(true)}
              variant="light" className="border m-1">
              <FontAwesomeIcon icon={faPlus} className="fa-lg"/> Request a kit
            </Button>
        }
      </div>
    </InfoCardHeader>
    {showRequestKitModal && <RequestKitModal
      studyEnvContext={studyEnvContext}
      enrolleeShortcode={enrollee.shortcode}
      onDismiss={() => setShowRequestKitModal(false)}
      onSubmit={onSubmit}
    />}
    {basicTableLayout(table, { tableClass: 'table m-0' })}
    {enrollee.kitRequests.length === 0 && <div className='my-3'>
      {renderEmptyMessage(enrollee.kitRequests, 'No kit requests')}
    </div>}
  </InfoCard>
}
