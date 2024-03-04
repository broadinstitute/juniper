import { AdminTask, AdminUser, Enrollee, ParticipantNote } from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretDown, faCheck, faUser } from '@fortawesome/free-solid-svg-icons'
import { instantToDateString, instantToDefaultString } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { enrolleeKitRequestPath } from './EnrolleeView'
import React, { useState } from 'react'
import { AdminTaskEditModal } from 'study/adminTasks/AdminTaskEditor'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'


type ParticipantNoteViewProps = {
  enrollee: Enrollee
  note: ParticipantNote
  studyEnvContext: StudyEnvContextT
  linkedTasks: AdminTask[]
  users: AdminUser[]
  reloadTasks: () => void
}
/** renders a single note, with the admin user name and a link to the kit request, if it exists */
export function ParticipantNoteView({
  enrollee, note, users, studyEnvContext, linkedTasks,
  reloadTasks
}: ParticipantNoteViewProps) {
  const matchedUser = users.find(user => user.id === note.creatingAdminUserId)
  /**
   * we might not have the username if the user list hasn't loaded, or if the user is no longer associated with the
   * portal. eventually, we might want more elegant handling, for now, just show the id instead
   */
  const usernameString = matchedUser?.username ?? `superuser (${note.creatingAdminUserId.slice(-4)})`
  const matchedTask = linkedTasks.find(task => task.participantNoteId === note.id)
  const [showTaskEdit, setShowTaskEdit] = useState(false)
  const matchedAssignedUser = users.find(user => user.id === matchedTask?.assignedAdminUserId)
  return <div className="mb-3">
    <div className="d-flex align-items-center">
      <div className="fw-bold text-muted">
        <FontAwesomeIcon icon={faUser} className="mx-2"/>
        {usernameString}
      </div>
      <div className="text-muted ms-3">{instantToDefaultString(note.createdAt)}</div>
      { matchedTask && <button className="badge ms-auto btn-secondary"
        style={{ backgroundColor: '#eee' }}
        onClick={() => setShowTaskEdit(!showTaskEdit)}>
        { matchedTask.status === 'NEW' && <span>
          { matchedAssignedUser ?
              `Assigned: ${matchedAssignedUser.username}` : 'Unassigned' }
          <FontAwesomeIcon icon={faCaretDown} className="fa-sm ms-2"/>
        </span> }
        { matchedTask.status === 'COMPLETE' && <span>
          <FontAwesomeIcon icon={faCheck} className="fa-sm me-2"/>
            Resolved: {instantToDateString(matchedTask.completedAt)}
        </span> }

      </button>
      }
    </div>
    { (showTaskEdit && matchedTask) && <div>
      <AdminTaskEditModal task={matchedTask} users={users}
        onDismiss={() => {
          setShowTaskEdit(false)
          reloadTasks()
        }}
        studyEnvContext={studyEnvContext}/>
    </div> }
    <div className="mt-1">{note.text}</div>
    { note.kitRequestId && <Link to={enrolleeKitRequestPath(studyEnvContext.currentEnvPath, enrollee.shortcode)}>
      Kit requests
    </Link>}
  </div>
}


