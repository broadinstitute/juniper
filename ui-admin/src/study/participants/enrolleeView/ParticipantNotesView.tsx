import React, { useState } from 'react'
import Api from 'api/api'
import { useAdminUserContext } from 'providers/AdminUserProvider'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import {
  Enrollee,
  ParticipantNote,
  ParticipantTask
} from '@juniper/ui-core'
import { ParticipantNoteModal } from './ParticipantNoteModal'
import { renderEmptyMessage } from 'util/tableUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { Button } from 'components/forms/Button'
import {
  InfoCard,
  InfoCardHeader
} from 'components/InfoCard'
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
  const [linkedTasks, setLinkedTasks] = useState<ParticipantTask[]>([])
  const { users } = useAdminUserContext()
  const sortedNotes = [...notes].sort((a, b) => b.createdAt - a.createdAt)

  const { reload: reloadTasks } = useLoadingEffect(async () => {
    const tasks = await Api.fetchEnrolleeAdminTasks(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, enrollee.shortcode)
    setLinkedTasks(tasks)
  }, [enrollee.shortcode])

  return <InfoCard>
    <InfoCardHeader>
      <div className="d-flex align-items-center justify-content-between w-100">
        <div className="fw-bold lead my-1">Notes</div>
        <Button onClick={() => setShowAdd(!showAdd)}
          variant="light" className="border m-1">
          <FontAwesomeIcon icon={faPlus} className="fa-lg"/> Create new note
        </Button>
      </div>
    </InfoCardHeader>
    {sortedNotes.length > 0 && sortedNotes.map((note, index) => (<div key={index}>
      <div className='p-3'>
        <ParticipantNoteView enrollee={enrollee} note={note}
          studyEnvContext={studyEnvContext}
          linkedTasks={linkedTasks}
          reloadTasks={reloadTasks}
          users={users}
          key={note.id}/>
      </div>
      <hr className='m-0'/>
    </div>
    ))}
    {sortedNotes.length === 0 && <div className='my-3'>{renderEmptyMessage(sortedNotes, 'No notes')}</div>}
    { showAdd && <ParticipantNoteModal enrollee={enrollee} users={users}
      onDismiss={async () => {
        await reloadTasks()
        onUpdate()
        setShowAdd(false)
      }} studyEnvContext={studyEnvContext} />}
  </InfoCard>
}

export default ParticipantNotesView
