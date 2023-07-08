import React, {useState} from 'react'
import Api, {AdminUser, Enrollee, ParticipantNote} from "api/api";
import {instantToDefaultString} from "util/timeUtils";
import {useAdminUserContext} from "providers/AdminUserProvider";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus, faUser} from "@fortawesome/free-solid-svg-icons";
import {enrolleeKitRequestPath} from "./EnrolleeView";
import {Link} from "react-router-dom";
import {StudyEnvContextT} from "../../StudyEnvironmentRouter";
import {failureNotification} from "../../../util/notifications";
import {Store} from "react-notifications-component";

type ParticipantNotesViewProps = {
  enrollee: Enrollee,
  notes: ParticipantNote[],
  studyEnvContext: StudyEnvContextT,
  onUpdate: () => void
}

export default function ParticipantNotesView({enrollee, notes, studyEnvContext, onUpdate}: ParticipantNotesViewProps) {
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
      <ParticipantNoteView enrollee={enrollee} note={note} studyEnvContext={studyEnvContext} users={users}
        key={note.id}/>
    )}
  </div>
}

type ParticipantNoteViewProps = {
  enrollee: Enrollee,
  note: ParticipantNote,
  studyEnvContext: StudyEnvContextT,
  users: AdminUser[]
}
function ParticipantNoteView({ enrollee, note, users, studyEnvContext }: ParticipantNoteViewProps) {
    return <div className="mb-3">
      <div>
        <span className="fw-bold text-muted"> <FontAwesomeIcon icon={faUser} className="mx-2"/>
        {users.find(user => user.id === note.creatingAdminUserId)?.username}
      </span>
        <span className="text-muted ms-3">{instantToDefaultString(note.createdAt)}</span>
      </div>
      <div className="mt-1">{note.text}</div>
      { note.kitRequestId && <Link to={enrolleeKitRequestPath(studyEnvContext.currentEnvPath, enrollee.shortcode)}>
        Kit requests
      </Link>}
    </div>
}
