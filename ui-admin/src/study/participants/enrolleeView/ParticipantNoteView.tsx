import { AdminUser, Enrollee, ParticipantNote } from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUser } from '@fortawesome/free-solid-svg-icons'
import { instantToDefaultString } from 'util/timeUtils'
import { Link } from 'react-router-dom'
import { enrolleeKitRequestPath } from './EnrolleeView'
import React from 'react'


type ParticipantNoteViewProps = {
  enrollee: Enrollee,
  note: ParticipantNote,
  currentEnvPath: string,
  users: AdminUser[]
}
/** renders a single note, with the admin user name and a link to the kit request, if it exists */
export function ParticipantNoteView({ enrollee, note, users, currentEnvPath }: ParticipantNoteViewProps) {
  const matchedUser = users.find(user => user.id === note.creatingAdminUserId)
  /**
   * we might not have the username if the user list hasn't loaded, or if the user is no longer associated with the
   * portal. eventually, we might want more elegant handling, for now, just show the id instead
   */
  const usernameString = matchedUser?.username ?? note.creatingAdminUserId
  return <div className="mb-3">
    <div>
      <span className="fw-bold text-muted">
        <FontAwesomeIcon icon={faUser} className="mx-2"/>
        {usernameString}
      </span>
      <span className="text-muted ms-3">{instantToDefaultString(note.createdAt)}</span>
    </div>
    <div className="mt-1">{note.text}</div>
    { note.kitRequestId && <Link to={enrolleeKitRequestPath(currentEnvPath, enrollee.shortcode)}>
      Kit requests
    </Link>}
  </div>
}
