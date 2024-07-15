import React, { useId, useState } from 'react'
import Api from 'api/api'
import { AdminUser } from 'api/adminUser'
import { Modal } from 'react-bootstrap'
import Select from 'react-select'
import LoadingSpinner from 'util/LoadingSpinner'
import AdminUserSelect from 'user/AdminUserSelect'
import { Enrollee, instantToDefaultString, ParticipantTask, ParticipantTaskStatus } from '@juniper/ui-core'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Textarea } from 'components/forms/Textarea'
import { doApiLoad } from 'api/api-utils'


/** Form for editing an admin task */
export default function ParticipantNoteEditor({ task, workingTask, setWorkingTask, users }: {
  task: ParticipantTask, workingTask: ParticipantTask,
  setWorkingTask: (task: ParticipantTask) => void, users: AdminUser[]}
) {
  const selectedUser = users.find(user => user.id === workingTask.assignedAdminUserId)
  const statusSelectId = useId()
  const userSelectId = useId()
  const setSelectedUser = ((user: AdminUser | undefined) => {
    setWorkingTask({
      ...workingTask,
      assignedAdminUserId: user?.id
    })
  })
  const statusOpts: {label: string, value: ParticipantTaskStatus}[] = ['NEW', 'COMPLETE']
    .map(status => ({ label: status, value: status as ParticipantTaskStatus }))
  const statusValue = statusOpts.find(opt => opt.value === workingTask.status)
  return <div>
    <label className="mt-3" htmlFor={userSelectId}>Assigned to</label>
    <AdminUserSelect selectedUser={selectedUser} setSelectedUser={setSelectedUser} id={userSelectId}
      users={users} readOnly={task.status === 'COMPLETE'}/>

    { task.status !== 'COMPLETE' && <div className="mt-3">
      <label htmlFor={statusSelectId}>Status</label>
      <Select options={statusOpts} value={statusValue} inputId={statusSelectId}
        styles={{ control: baseStyles => ({ ...baseStyles, width: '400px' }) }}
        onChange={opt => setWorkingTask({
          ...workingTask, status: opt?.value ?? 'NEW'
        })}/>
    </div> }
    { task.status === 'COMPLETE' && <div className="mt-3">
            Completed {instantToDefaultString(task.completedAt)}
    </div> }

  </div>
}

/**
 * shows a modal for editing the passed-in task.  this handles saving the task to the server.
 * If the task was saved, the updated task will be passed to the onDismiss handler
 */
export const ParticipantNoteModal = ({ enrollee, users, onDismiss, studyEnvContext }: {
  enrollee: Enrollee, users: AdminUser[], onDismiss: (task?: ParticipantTaskStatus) => void,
  studyEnvContext: StudyEnvContextT
}) => {
  const [isLoading, setIsLoading] = useState(false)
  const userSelectId = useId()
  const [newNoteText, setNewNoteText] = useState('')
  const [newNoteAssignee, setNewNoteAssignee] = useState<AdminUser>()

  const createNote = async () => {
    doApiLoad(async () => {
      await Api.createParticipantNote(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, enrollee.shortcode, {
          text: newNoteText, assignedAdminUserId: newNoteAssignee?.id
        })
      onDismiss()
      setNewNoteText('')
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create note</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="pb-2">
        <label className='form-label fw-semibold' htmlFor={userSelectId}>Assignee</label>
        <AdminUserSelect selectedUser={newNoteAssignee} setSelectedUser={setNewNoteAssignee} users={users}
          readOnly={false} id={userSelectId}/>
      </div>
      <Textarea label='Note' value={newNoteText} onChange={e => setNewNoteText(e)} rows={5}/>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={createNote}>Save</button>
        <button className="btn btn-secondary" onClick={() => onDismiss()}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
