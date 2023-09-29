import React, { useId, useState } from 'react'
import Api, { AdminTask, AdminUser, Enrollee, ParticipantNote } from 'api/api'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { ParticipantNoteView } from './ParticipantNoteView'
import AdminUserSelect from 'user/AdminUserSelect'
import { useLoadingEffect } from 'api/api-utils'

type ParticipantNotesViewProps = {
  enrollee: Enrollee,
  notes: ParticipantNote[],
  studyEnvContext: StudyEnvContextT,
  onUpdate: () => void
}

/** shows a list of participant notes, with the ability to add a new note */
const ParticipantNotesView = ({ enrollee, notes, studyEnvContext, onUpdate }: ParticipantNotesViewProps) => {
  const [showAdd, setShowAdd] = useState(false)
  const [linkedTasks, setLinkedTasks] = useState<AdminTask[]>([])
  const [newNoteText, setNewNoteText] = useState('')
  const [newNoteAssignee, setNewNoteAssignee] = useState<AdminUser>()
  const { users } = useAdminUserContext()
  const sortedNotes = [...notes].sort((a, b) => b.createdAt - a.createdAt)
  const userSelectId = useId()

  const { reload: reloadTasks } = useLoadingEffect(async () => {
    const tasks = await Api.fetchEnrolleeAdminTasks(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, enrollee.shortcode)
    setLinkedTasks(tasks)
  }, [enrollee.shortcode])

  const createNote = async () => {
    try {
      await Api.createParticipantNote(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.shortcode, {
          text: newNoteText, assignedAdminUserId: newNoteAssignee?.id
        })
      setShowAdd(false)
      setNewNoteText('')
    } catch (e) {
      Store.addNotification(failureNotification('could not save note'))
    }
    onUpdate()
    reloadTasks()
  }

  return <div>
    <h3 className="h4">Notes</h3>
    <button className="btn btn-secondary" onClick={() => setShowAdd(!showAdd)}>
      <FontAwesomeIcon icon={faPlus}/> Add
    </button>
    {showAdd && <div className="pb-3">
      <textarea rows={5} cols={80} value={newNoteText} onChange={e => setNewNoteText(e.target.value)}/>
      <div>
        <label htmlFor={userSelectId}>Assign to:</label>
        <AdminUserSelect selectedUser={newNoteAssignee} setSelectedUser={setNewNoteAssignee} users={users}
          readOnly={false} id={userSelectId}/>
      </div>
      <div className="mt-2">
        <button className="btn btn-primary" onClick={createNote}>Save</button>
      </div>
    </div>}
    { sortedNotes.map(note =>
      <ParticipantNoteView enrollee={enrollee} note={note}
        studyEnvContext={studyEnvContext}
        linkedTasks={linkedTasks}
        reloadTasks={reloadTasks}
        users={users}
        key={note.id}/>
    )}
  </div>
}

export default ParticipantNotesView

