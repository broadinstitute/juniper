import React, { useState } from 'react'
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
import { basicTableLayout, IndeterminateCheckbox, renderEmptyMessage, RowVisibilityCount } from 'util/tableUtils'
import { currentIsoDate, instantToDateString, instantToDefaultString } from '@juniper/ui-core'
import { Button } from 'components/forms/Button'
import { escapeCsvValue, saveBlobAsDownload } from 'util/downloadUtils'
import { failureNotification, successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import Modal from 'react-bootstrap/Modal'
import { useLoadingEffect } from '../api/api-utils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faAdd, faDownload, faTrash } from '@fortawesome/free-solid-svg-icons'
import { renderPageHeader } from 'util/pageUtils'
import { useFileUploadButton } from '../util/uploadUtils'


/** show the mailing list in table */
export default function MailingListView({ portalContext, portalEnv }:
{portalContext: LoadedPortalContextT, portalEnv: PortalEnvironment}) {
  const [contacts, setContacts] = useState<MailingListContact[]>([])
  const [sorting, setSorting] = React.useState<SortingState>([{ 'id': 'createdAt', 'desc': true }])
  const [rowSelection, setRowSelection] = React.useState<Record<string, boolean>>({})
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [showAddUsersModal, setShowAddUsersModal] = useState(false)
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
    onRowSelectionChange: setRowSelection
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

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.fetchMailingList(portalContext.portal.shortcode, portalEnv.environmentName)
    setContacts(result)
  }, [portalContext.portal.shortcode, portalEnv.environmentName])

  const performDelete = async () => {
    const contactsSelected = Object.keys(rowSelection)
      .filter(key => rowSelection[key])
      .map(key => contacts[parseInt(key)])
    try {
      // this might get gnarly with more than a few entries, but that's okay for now -- this is not expected to be
      // a heavy-use feature.
      await Promise.all(
        contactsSelected.map(contact =>
          Api.deleteMailingListContact(portalContext.portal.shortcode, portalEnv.environmentName, contact.id!)
        )
      )
      Store.addNotification(successNotification(`${contactsSelected.length} entries removed`))
    } catch {
      Store.addNotification(failureNotification('Error: some entries could not be removed'))
    }
    reload() // just reload the whole thing to be safe
    setShowDeleteConfirm(false)
  }

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Mailing List') }
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex">
          <RowVisibilityCount table={table}/>
        </div>
        <div className="d-flex">
          <Button onClick={() => setShowAddUsersModal(!showAddUsersModal)}
            variant="light" className="border m-1">
            <FontAwesomeIcon icon={faAdd} className="fa-lg"/> Add Users
          </Button>
          <Button onClick={download}
            variant="light" className="border m-1" disabled={!numSelected}
            tooltip={numSelected ? 'Download selected contacts' : 'You must select contacts to download'}>
            <FontAwesomeIcon icon={faDownload} className="fa-lg"/> Download
          </Button>
          <Button onClick={() => setShowDeleteConfirm(!showDeleteConfirm)}
            variant="light" className="border m-1" disabled={!numSelected}
            tooltip={numSelected ? 'Remove selected contacts' : 'You must select contacts to remove'}>
            <FontAwesomeIcon icon={faTrash} className="fa-lg"/> Remove
          </Button>
        </div>
      </div>

      { basicTableLayout(table) }
      { renderEmptyMessage(contacts, 'No contacts') }
      { showDeleteConfirm && <Modal show={true} onHide={() => setShowDeleteConfirm(false)}>
        <Modal.Body>
          <div>Do you want to delete the <strong>{ numSelected }</strong> selected entries?</div>

          <div className="pt-3">This operation CANNOT BE UNDONE.</div>
        </Modal.Body>
        <Modal.Footer>
          <button type="button" className="btn btn-danger" onClick={performDelete}>
            Delete
          </button>
          <button type="button" className="btn btn-secondary" onClick={() => setShowDeleteConfirm(false)}>
            Cancel
          </button>
        </Modal.Footer>
      </Modal> }
      <AddUsersModal
        portalContext={portalContext}
        portalEnv={portalEnv}
        show={showAddUsersModal}
        reload={reload}
        onClose={() => setShowAddUsersModal(false)}
      />
    </LoadingSpinner>
  </div>
}

/**
 * Modal for adding users to the mailing list.
 */
export function AddUsersModal({ portalContext, portalEnv, show, onClose, reload }: {
  portalContext: LoadedPortalContextT,
  portalEnv: PortalEnvironment,
  show: boolean,
  onClose: () => void,
  reload: () => void
}) {
  const [emails, setEmails] = useState<MailingListContact[]>([{ name: '', email: '' }])
  const [isLoading, setIsLoading] = useState(false)
  const { FileChooser, file } = useFileUploadButton(file => {
    const reader = new FileReader()
    reader.onload = () => {
      setEmails(parseCsv(reader.result as string))
    }
    reader.readAsText(file)
  }, 'Import CSV')

  const hasInvalidContacts = emails.some(contact => !contact.email && contact.name)

  const addUsers = async () => {
    setIsLoading(true)
    const validContacts = emails.filter(contact => contact.email && contact.name)
    try {
      await Api.addMailingListContacts(portalContext.portal.shortcode, portalEnv.environmentName, validContacts)
      Store.addNotification(successNotification('Users added'))
      onClose()
    } catch {
      Store.addNotification(failureNotification('Error: could not add users'))
    }
    reload()
    setIsLoading(false)
  }

  return <Modal show={show} className="modal-lg" onHide={onClose}>
    <Modal.Header closeButton>
      <Modal.Title>Add Users</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="pb-3">
        Add users to your mailing list by entering their name and email address. You
        can also import a <code>.csv</code> file with multiple contacts. The file should
        be formatted as <code>name,emailAddress</code>.
      </div>
      <table className="ms-2 table">
        <thead>
          <tr>
            <td className="fw-semibold">Name</td>
            <td className="fw-semibold">Email Address*</td>
            <td></td>
          </tr>
        </thead>
        <tbody>
          {emails.map((contact, i) =>
            <ContactEntry
              key={i}
              contact={contact}
              onChange={newContact => {
                setEmails(emails => [
                  ...emails.slice(0, i),
                  { ...emails[i], ...newContact },
                  ...emails.slice(i + 1)
                ])
              }}
              onRemove={() =>
                setEmails(emails => [
                  ...emails.slice(0, i),
                  ...emails.slice(i + 1)
                ])
              }
            />
          )}
        </tbody>
      </table>
      <button className="btn btn-primary" onClick={() => setEmails([...emails, { name: '', email: '' }])}>
            Add
      </button>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        {FileChooser}
        <Button
          disabled={hasInvalidContacts}
          tooltip={hasInvalidContacts ? 'All contacts must have an email address' : undefined}
          onClick={addUsers} variant="primary">
        Save
        </Button>
        <Button onClick={onClose} variant="secondary">
        Cancel
        </Button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

function ContactEntry({ contact, onRemove, onChange }: {
  contact: MailingListContact,
  onRemove: () => void,
  onChange: (contact: MailingListContact) => void
}) {
  return (
    <tr>
      <td>
        <input
          type="text"
          className="form-control mb-1"
          placeholder={'Name'}
          value={contact.name ?? ''}
          onChange={e => onChange({ ...contact, name: e.target.value })}
        />
      </td>
      <td>
        <input
          type="text"
          className="form-control mb-1"
          placeholder={'Email Address'}
          value={contact.email ?? ''}
          onChange={e => onChange({ ...contact, email: e.target.value })}
        />
      </td>
      <td>
        <Button variant="secondary">
          <FontAwesomeIcon icon={faTrash} className="fa-lg" onClick={onRemove}/>
        </Button>
      </td>
    </tr>
  )
}

function parseCsv(csv: string): MailingListContact[] {
  return csv.split('\n').map(line => {
    const [name, email] = line.split(',')
    return { name, email }
  })
}
