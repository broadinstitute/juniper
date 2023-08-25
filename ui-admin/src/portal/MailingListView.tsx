import React, { useEffect, useState } from 'react'
import Api, { MailingListContact, PortalEnvironment } from 'api/api'
import { LoadedPortalContextT } from './PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  ColumnDef,
  getCoreRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable
} from '@tanstack/react-table'
import { basicTableLayout, IndeterminateCheckbox } from 'util/tableUtils'
import { currentIsoDate, instantToDateString, instantToDefaultString } from 'util/timeUtils'
import { Button } from 'components/forms/Button'
import { escapeCsvValue, saveBlobAsDownload } from 'util/downloadUtils'
import { failureNotification, successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import Modal from 'react-bootstrap/Modal'


/** show the mailing list in table */
export default function MailingListView({ portalContext, portalEnv }:
{portalContext: LoadedPortalContextT, portalEnv: PortalEnvironment}) {
  const [contacts, setContacts] = useState<MailingListContact[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const columns: ColumnDef<MailingListContact>[] = [{
    id: 'select',
    header: ({ table }) => <IndeterminateCheckbox
      checked={table.getIsAllRowsSelected()} indeterminate={table.getIsSomeRowsSelected()}
      onChange={table.getToggleAllRowsSelectedHandler()}/>,
    cell: ({ row }) => (
      <div className="px-1">
        <IndeterminateCheckbox
          checked={row.getIsSelected()} indeterminate={row.getIsSomeSelected()}
          onChange={row.getToggleSelectedHandler()} disabled={!row.getCanSelect()}/>
      </div>
    )
  }, {
    header: 'Email',
    accessorKey: 'email'
  }, {
    header: 'Name',
    accessorKey: 'name'
  }, {
    header: 'Joined',
    accessorKey: 'createdAt',
    cell: info => instantToDefaultString(info.getValue() as number)
  }]


  const table = useReactTable({
    data: contacts,
    columns,
    state: {
      sorting,
      rowSelection
    },
    enableRowSelection: true,
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onRowSelectionChange: setRowSelection,
    debugTable: true
  })

  /** download selected contacts as a csv */
  const download = () => {
    const contactsSelected = Object.keys(rowSelection)
      .filter(key => rowSelection[key])
      .map(key => contacts[parseInt(key)])
    const csvDataString = contactsSelected.map(contact => {
      return `${escapeCsvValue(contact.email)}, ${escapeCsvValue(contact.name)}, 
      ${instantToDateString(contact.createdAt)}`
    }).join('\n')
    const csvString = `email, name, date joined\n${  csvDataString}`
    const blob = new Blob([csvString], {
      type: 'text/plain'
    })
    saveBlobAsDownload(blob, `${portalContext.portal.shortcode}-MailingList-${currentIsoDate()}.csv`)
  }
  const numSelected = Object.keys(rowSelection).length

  const loadMailingList = () => {
    setIsLoading(true)
    setRowSelection({})
    Api.fetchMailingList(portalContext.portal.shortcode, portalEnv.environmentName).then(result => {
      setContacts(result)
      setIsLoading(false)
    }).catch((e: Error) => {
      alert(`error loading mailing list ${  e}`)
      setIsLoading(false)
    })
  }

  const performDelete = async () => {
    const contactsSelected = Object.keys(rowSelection)
      .filter(key => rowSelection[key])
      .map(key => contacts[parseInt(key)])
    try {
      // this might get gnarly with more than a few entries, but that's okay for now -- this is not expected to be
      // a heavy-use feature.
      await Promise.all(
        contactsSelected.map(contact =>
          Api.deleteMailingListContact(portalContext.portal.shortcode, portalEnv.environmentName, contact.id)
        )
      )
      Store.addNotification(successNotification(`${contactsSelected.length} entries removed`))
    } catch {
      Store.addNotification(failureNotification('Error: some entries could not be removed'))
    }
    loadMailingList() // just reload the whole thing to be safe
    setShowDeleteConfirm(false)
  }

  useEffect(() => {
    loadMailingList()
  }, [portalContext.portal.shortcode, portalEnv.environmentName])

  return <div className="container p-3">
    <h1 className="h4">Mailing list </h1>
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center">
        <div>
          {numSelected} of {table.getPreFilteredRowModel().rows.length} selected
        </div>
        <Button onClick={download}
          variant="secondary" disabled={!numSelected}
          tooltip={numSelected ? 'Download selected contacts' : 'you must select contacts to download'}>
          Download
        </Button>
        <Button onClick={() => setShowDeleteConfirm(!showDeleteConfirm)}
          variant="secondary" disabled={!numSelected} className="ms-auto"
          tooltip={numSelected ? 'Remove selected contacts' : 'you must select contacts to remove'}>
          Remove
        </Button>
      </div>

      {basicTableLayout(table)}
      { showDeleteConfirm && <Modal show={true} onHide={() => setShowDeleteConfirm(false)}>
        <Modal.Body>
          <div>Do you want to delete the <strong>{ numSelected }</strong> selected entries?</div>

          <div className="pt-3">This operation CANNOT BE UNDONE.</div>
        </Modal.Body>
        <Modal.Footer>
          <button type="button" className="btn btn-primary" onClick={performDelete}>
            Delete
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => setShowDeleteConfirm(false)}>
            Cancel
          </button>
        </Modal.Footer>
      </Modal> }
    </LoadingSpinner>
  </div>
}
