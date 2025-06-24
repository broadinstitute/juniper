import React from 'react'
import { ParticipantUser } from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { InfoCardRow } from 'components/InfoCard'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPencil } from '@fortawesome/free-solid-svg-icons'
import Api from 'api/api'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'
import Modal from 'react-bootstrap/Modal'

export const UsernameEditor = ({ participantUser, studyEnvContext, onUpdate }: {
  participantUser: ParticipantUser,
  studyEnvContext: StudyEnvContextT,
  onUpdate: () => void
}) => {
  const [editMode, setEditMode] = React.useState(false)
  const [username, setUsername] = React.useState(participantUser.username)
  const [showConfirm, setShowConfirm] = React.useState(false)
  const unchanged = participantUser.username === username

  const updateUsername = async (newUsername: string) => {
    try {
      await Api.updateParticipantUser(
        studyEnvContext.portal.shortcode,
        studyEnvContext.currentEnv.environmentName,
        participantUser.id,
        { username: newUsername }
      )
      onUpdate()
      setEditMode(false)
      Store.addNotification(successNotification(`Username successfully updated to ${newUsername}`))
    } catch (error) {
      Store.addNotification(
        failureNotification(`Failed to update username${  error instanceof Error ? `: ${error.message}` : ''}`)
      )
    }
  }

  return <InfoCardRow title={'Username'} condensed={true}>
    <div className='d-flex align-items-end h-100 m-0'>
      {editMode ? <>
        <input
          type="text"
          className="form-control form-control-sm w-50 me-2"
          defaultValue={username}
          onChange={e => {
            setUsername(e.target.value)
          }}
        />
        <button
          className="btn btn-sm btn-primary me-2"
          onClick={() => {
            setShowConfirm(true)
          }}
          disabled={unchanged}
        >
            Save
        </button>
        <button
          className="btn btn-sm btn-secondary"
          onClick={() => setEditMode(false)}
        >
            Cancel
        </button>
      </>
        :<>
          <span className="m-0">{participantUser.username}</span>
          <button
            className="btn btn-sm ms-2 p-0"
            onClick={() => {
              setEditMode(true)
            }}
          >
            <FontAwesomeIcon icon={faPencil}/>
          </button>
        </>}
    </div>
    {showConfirm && <ConfirmUsernameChange
      oldUsername={participantUser.username}
      newUsername={username}
      onConfirm={() => {
        updateUsername(username)
        setShowConfirm(false)
      }}
      onCancel={() => setShowConfirm(false)}/>
    }
  </InfoCardRow>
}

const ConfirmUsernameChange = ({ oldUsername, newUsername, onConfirm, onCancel }: {
  oldUsername: string,
  newUsername: string,
  onConfirm: () => void,
  onCancel: () => void
}) => {
  return <Modal show={true} onHide={onCancel}>
    <Modal.Header closeButton>
      <Modal.Title>Confirm Username Change</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>
        Are you sure you want to change the username from <strong>
          {oldUsername}
        </strong> to <strong>
          {newUsername}
        </strong>?</p>

      { !simpleEmailValidate(newUsername) &&
      <p className="mx-2 text-danger">
        warning: {newUsername} does not appear to be a valid email address.
      </p>}
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      <button className="btn btn-primary" onClick={onConfirm}>Confirm</button>
    </Modal.Footer>

  </Modal>
}

const simpleEmailValidate = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}
