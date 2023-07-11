import React, { useState } from 'react'
import Api, { Enrollee, ParticipantNote } from 'api/api'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { failureNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import { ParticipantNoteView } from './ParticipantNoteView'

type ParticipantNotesViewProps = {
  enrollee: Enrollee,
  notes: ParticipantNote[],
  studyEnvContext: StudyEnvContextT,
  onUpdate: () => void
}

/** shows a list of participant notes, with the ability to add a new note */
const ParticipantNotesView = ({ enrollee, notes, studyEnvContext, onUpdate }: ParticipantNotesViewProps) => {
  const [showAdd, setShowAdd] = useState(false)
  const [newNoteText, setNewNoteText] = useState('')
  const { users } = useAdminUserContext()
  const sortedNotes = [...notes].sort((a, b) => b.createdAt - a.createdAt)

  const createNote = async () => {
    try {
      await Api.createParticipantNote(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.shortcode, newNoteText)
      setShowAdd(false)
      setNewNoteText('')
    } catch (e) {
      Store.addNotification(failureNotification('could not save note'))
    }


    onUpdate()
  }
  return <div>
    <h3 className="h6">Notes
      <button className="btn btn-secondary" onClick={() => setShowAdd(!showAdd)}>
        <FontAwesomeIcon icon={faPlus}/> Add
      </button>
    </h3>
    {showAdd && <div className="pb-3">
      <textarea rows={5} cols={80} value={newNoteText} onChange={e => setNewNoteText(e.target.value)}/>
      <div>
        <button className="btn btn-primary" onClick={createNote}>Save</button>
      </div>
    </div>}
    { sortedNotes.map(note =>
      <ParticipantNoteView enrollee={enrollee} note={note} currentEnvPath={studyEnvContext.currentEnvPath} users={users}
        key={note.id}/>
    )}
  </div>
}

export default ParticipantNotesView

