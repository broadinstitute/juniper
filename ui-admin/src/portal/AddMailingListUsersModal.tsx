import Api, { MailingListContact, PortalEnvironment } from '../api/api'
import React, { useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTrash } from '@fortawesome/free-solid-svg-icons'
import { Button } from '../components/forms/Button'
import LoadingSpinner from 'util/LoadingSpinner'
import { Modal } from 'react-bootstrap'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from '../util/notifications'
import { useFileUploadButton } from '../util/uploadUtils'
import { LoadedPortalContextT } from './PortalProvider'
import pluralize from 'pluralize'

/**
 * Modal for adding users to the mailing list.
 */
export function AddMailingListUsersModal({ portalContext, portalEnv, show, onClose, reload }: {
    portalContext: LoadedPortalContextT,
    portalEnv: PortalEnvironment,
    show: boolean,
    onClose: () => void,
    reload: () => void
}) {
  const [contacts, setContacts] = useState<MailingListContact[]>([{ name: '', email: '' }])
  const [isLoading, setIsLoading] = useState(false)
  const { FileChooser } = useFileUploadButton(file => {
    const reader = new FileReader()
    reader.onload = () => {
      setContacts(parseContactCsv(reader.result as string))
    }
    reader.readAsText(file)
  }, 'Import CSV')

  //A contact is invalid if it has a name but no email, or an email that doesn't match the email regex.
  //Contacts without names are allowed.
  const hasInvalidContacts = contacts.some(contact =>
    (!contact.email && contact.name) || (contact.email && !contact.email.match(/.+@.+\..+/)))

  const addUsers = async () => {
    setIsLoading(true)
    const nonEmptyContacts = contacts.filter(contact => contact.email && contact.name)
    try {
      const response = await Api.addMailingListContacts(
        portalContext.portal.shortcode, portalEnv.environmentName, nonEmptyContacts
      )
      Store.addNotification(successNotification(`${response.length} new ${pluralize('user', response.length)} added`))
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
          {contacts.map((contact, i) =>
            <ContactEntry
              key={i}
              contact={contact}
              onChange={newContact => {
                setContacts(emails => [
                  ...emails.slice(0, i),
                  { ...emails[i], ...newContact },
                  ...emails.slice(i + 1)
                ])
              }}
              onRemove={() =>
                setContacts(emails => [
                  ...emails.slice(0, i),
                  ...emails.slice(i + 1)
                ])
              }
            />
          )}
        </tbody>
      </table>
      <button className="btn btn-primary"
        onClick={() =>
          setContacts([...contacts, { name: '', email: '' }])
        }>
        Add
      </button>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        {FileChooser}
        <Button
          disabled={hasInvalidContacts}
          tooltip={hasInvalidContacts ? 'All contacts must have a valid email address' : undefined}
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

/**
 * Parses a CSV string into a list of mailing list contacts.
 */
export function parseContactCsv(csv: string): MailingListContact[] {
  return csv.split('\n').map(line => {
    const [name, email] = line.split(',')
    return { name, email }
  })
}
