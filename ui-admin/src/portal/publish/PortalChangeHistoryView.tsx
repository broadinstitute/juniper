import React from 'react'
import { instantToDefaultString, Portal } from '@juniper/ui-core'
import { renderPageHeader } from 'util/pageUtils'
import { useLoadingEffect } from 'api/api-utils'
import Api, { PortalEnvironmentChangeRecord } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { basicTableLayout, renderEmptyMessage } from 'util/tableUtils'
import Modal from 'react-bootstrap/Modal'
import _truncate from 'lodash/truncate'
import { Button } from 'components/forms/Button'

/** shows the list of PortalEnvironmentChangeRecords for a portal */
export default function PortalChangeHistoryView({ portal }: {portal: Portal}) {
  const [records, setRecords] = React.useState<PortalEnvironmentChangeRecord[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [modalRecord, setModalRecord] = React.useState<PortalEnvironmentChangeRecord>()
  const { users } = useAdminUserContext()
  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchPortalEnvChangeRecords(portal.shortcode)
    result.forEach(record => {
      record.parsedChange = JSON.parse(record.portalEnvironmentChange)
    })
    setRecords(result)
  }, [portal.shortcode])

  const columns: ColumnDef<PortalEnvironmentChangeRecord>[] = [{
    header: 'Date',
    accessorKey: 'createdAt',
    meta: {
      columnType: 'instant'
    },
    cell: info => instantToDefaultString(info.getValue() as number)
  }, {
    header: 'Environment',
    accessorKey: 'environmentName'
  }, {
    header: 'Operator',
    accessorKey: 'adminUserId',
    cell: info => users.find(user => user.id === info.getValue())?.username
  }, {
    header: 'Changes',
    accessorKey: 'portalEnvironmentChange',
    cell: info => <span>{ _truncate(info.getValue() as string, { length: 70 }) }
      <Button variant="link" onClick={() => setModalRecord(info.row.original)}>detail</Button>
    </span>
  }]

  const table = useReactTable({
    data: records,
    columns,
    state: {
      sorting
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })


  return <div className="container">
    { renderPageHeader('Publishing history') }
    <LoadingSpinner isLoading={isLoading}>
      { basicTableLayout(table) }
      { renderEmptyMessage(records, 'No contacts') }
    </LoadingSpinner>
    { modalRecord && <Modal show={true} size={'xl'} onHide={() => setModalRecord(undefined)} className="modal-lg">
      <Modal.Header closeButton>
        <Modal.Title>Publish record</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <dl>
          <dt>Operator</dt>
          <dd>{ users.find(user => user.id === modalRecord.adminUserId)?.username }</dd>
          <dt>Date</dt>
          <dd>{ instantToDefaultString(modalRecord.createdAt) }</dd>
        </dl>
        <pre>
          { JSON.stringify(modalRecord.parsedChange, null, 2) }
        </pre>
      </Modal.Body>
      <Modal.Footer>
        <button className="btn btn-secondary" onClick={() => setModalRecord(undefined)}>Ok</button>
      </Modal.Footer>
    </Modal>
    }
  </div>
}
