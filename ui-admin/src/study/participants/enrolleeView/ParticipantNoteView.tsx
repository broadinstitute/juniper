import { AdminUser } from 'api/adminUser'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCheck, faClock, faPencil, faUser } from '@fortawesome/free-solid-svg-icons'
import {
  Enrollee,
  instantToDateString,
  instantToDefaultString,
  ParticipantNote,
  ParticipantTask
} from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { enrolleeKitRequestPath } from './EnrolleeView'
import React, { useState } from 'react'
import { AdminTaskEditModal } from 'study/adminTasks/AdminTaskEditor'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import classNames from 'classnames'


type ParticipantNoteViewProps = {
  enrollee: Enrollee
  note: ParticipantNote
  studyEnvContext: StudyEnvContextT
  linkedTasks: ParticipantTask[]
  users: AdminUser[]
  reloadTasks: () => Promise<void>
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
  const isComplete = matchedTask?.status === 'COMPLETE'
  return <div className="mb-2">
    <div className="d-flex align-items-start">
      <div>
        <div className="fw-bold text-muted">
          <FontAwesomeIcon icon={faUser} className="me-2"/>
          {usernameString}
        </div>
        <div className="text-muted">
          <FontAwesomeIcon icon={faClock} className="me-2"/>
          {instantToDefaultString(note.createdAt)}
        </div>
      </div>
      { matchedTask && <button className={classNames(
        isComplete ? 'badge bg-success' : 'badge bg-secondary',
        'ms-auto border-0 p-2'
      )}
      onClick={() => setShowTaskEdit(!showTaskEdit)}>
        { matchedTask.status === 'NEW' && <span>
          { matchedAssignedUser ?
              `Assigned: ${matchedAssignedUser.username}` : 'Unassigned' }
          <FontAwesomeIcon icon={faPencil} className="ms-2"/>
        </span> }
        { isComplete && <span>
          <FontAwesomeIcon icon={faCheck} className="me-2"/>
            Resolved: {instantToDateString(matchedTask.completedAt)}
        </span> }
      </button>
      }
    </div>
    { (showTaskEdit && matchedTask) && <div>
      <AdminTaskEditModal task={matchedTask} users={users}
        onDismiss={async () => {
          await reloadTasks()
          setShowTaskEdit(false)
        }}
        studyEnvContext={studyEnvContext}/>
    </div> }
    <div className="my-2">{note.text}</div>
    { note.kitRequestId && <Link to={enrolleeKitRequestPath(studyEnvContext.currentEnvPath, enrollee.shortcode)}>
      Kit requests
    </Link>}
  </div>
}
