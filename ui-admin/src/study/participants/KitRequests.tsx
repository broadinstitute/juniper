import React, { useState } from 'react'
import { Enrollee, KitRequest } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { ColumnDef, getCoreRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import RequestKitModal from './RequestKitModal'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from 'util/timeUtils'
import { useUser } from 'user/UserProvider'

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
  accessorKey: 'status'
}, {
  header: 'Created',
  accessorKey: 'createdAt',
  accessorFn: data => instantToDefaultString(data.createdAt)
}, {
  header: 'Address',
  cell: ({ row }) => <KitRequestAddress sentToAddressJson={row.original.sentToAddress}/>
}, {
  header: 'DSM Status',
  accessorKey: 'dsmStatus'
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

  const onSubmit = () => {
    setShowRequestKitModal(false)
    onUpdate()
  }

  const table = useReactTable({
    data: enrollee.kitRequests || [],
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  return <div>
    <h5>Kit requests</h5>
    { user.superuser &&
      <button className='btn btn-secondary' onClick={() => setShowRequestKitModal(true)}>
        <FontAwesomeIcon icon={faPlus}/> Create a kit request
      </button>
    }
    {showRequestKitModal && <RequestKitModal
      enrollee={enrollee}
      studyEnvContext={studyEnvContext}
      onDismiss={() => setShowRequestKitModal(false)}
      onSubmit={onSubmit}
    />}
    {basicTableLayout(table)}
  </div>
}
