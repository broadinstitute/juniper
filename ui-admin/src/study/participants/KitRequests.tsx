import React, { useState } from 'react'
import { Enrollee, KitRequest } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { ColumnDef, getCoreRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import RequestKitModal from './RequestKitModal'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from '@juniper/ui-core'
import { useUser } from 'user/UserProvider'
import InfoPopup from 'components/forms/InfoPopup'
import KitStatusCell from './KitStatusCell'

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
  cell: ({ row }) => <InfoPopup content={row.original.details || ''} placement='left'/>
}]

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

  return <div>
    <h2 className="h4">Kit requests</h2>
    { user.superuser &&
      <button className='btn btn-secondary' onClick={() => setShowRequestKitModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Create a kit request
      </button>
    }
    {showRequestKitModal && <RequestKitModal
      studyEnvContext={studyEnvContext}
      enrolleeShortcode={enrollee.shortcode}
      onDismiss={() => setShowRequestKitModal(false)}
      onSubmit={onSubmit}
    />}
    {basicTableLayout(table)}
  </div>
}
