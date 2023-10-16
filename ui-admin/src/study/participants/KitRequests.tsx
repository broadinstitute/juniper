import React, { useState } from 'react'
import Api, { Enrollee, KitRequest } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { ColumnDef, getCoreRowModel, useReactTable } from '@tanstack/react-table'
import { basicTableLayout } from 'util/tableUtils'
import RequestKitModal from './RequestKitModal'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from 'util/timeUtils'
import { useUser } from 'user/UserProvider'
import InfoPopup from 'components/forms/InfoPopup'
import KitStatusCell from './KitStatusCell'
import { ApiErrorResponse, defaultApiErrorHandle } from 'api/api-utils'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'

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
  header: 'DSM Status',
  accessorKey: 'dsmStatus',
  cell: ({ row }) => <InfoPopup content={row.original.dsmStatus} placement='left'/>
}]

/** Shows a list of all kit requests for an enrollee. */
export default function KitRequests({ enrollee, studyEnvContext, onUpdate }:
                                      {
                                        enrollee: Enrollee,
                                        studyEnvContext: StudyEnvContextT,
                                        onUpdate: () => void
                                      }) {
  const { currentEnv, portal, study } = studyEnvContext
  const { user } = useUser()
  const [showRequestKitModal, setShowRequestKitModal] = useState(false)

  const onSubmit = async (kitType: string) => {
    try {
      await Api.createKitRequest(portal.shortcode, study.shortcode,
        currentEnv.environmentName, enrollee.shortcode, kitType)
      setShowRequestKitModal(false)
      onUpdate()
    } catch (e) {
      if ((e as ApiErrorResponse).message.includes('ADDRESS_VALIDATION_ERROR')) {
        Store.addNotification(failureNotification(`
          Could not create kit request:  Address did not match any mailable address.\n\n  
          The Participant will need to update their address in order to receive a kit`))
      } else {
        defaultApiErrorHandle(e)
      }
    }
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
      onDismiss={() => setShowRequestKitModal(false)}
      onSubmit={onSubmit}
    />}
    {basicTableLayout(table)}
  </div>
}
