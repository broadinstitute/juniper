import React, { useState } from 'react'
import Api, { AdminTask, AdminTaskStatus, AdminUser } from 'api/api'
import { Modal } from 'react-bootstrap'
import Select from 'react-select'
import LoadingSpinner from 'util/LoadingSpinner'
import AdminUserSelect from 'user/AdminUserSelect'
import { doApiLoad } from 'api/api-utils'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { instantToDefaultString } from '../../util/timeUtils'


/** Form for editing an admin task */
export default function AdminTaskEditor({ task, workingTask, setWorkingTask, users }:
{ task: AdminTask, workingTask: AdminTask, setWorkingTask: (task: AdminTask) => void, users: AdminUser[]}) {
  const selectedUser = users.find(user => user.id === workingTask.assignedAdminUserId)
  const setSelectedUser = ((user: AdminUser | undefined) => {
    setWorkingTask({
      ...workingTask,
      assignedAdminUserId: user?.id
    })
  })
  const statusOpts: {label: string, value: AdminTaskStatus}[] = ['NEW', 'COMPLETE']
    .map(status => ({ label: status, value: status as AdminTaskStatus }))
  const statusValue = statusOpts.find(opt => opt.value === workingTask.status)
  return <div>
    <label className="mt-3">
      Assigned to:
      <AdminUserSelect selectedUser={selectedUser} setSelectedUser={setSelectedUser}
        users={users} readOnly={task.status === 'COMPLETE'}/>
    </label>
    { task.status !== 'COMPLETE' && <label className="mt-3">
            Status:
      <Select options={statusOpts} value={statusValue}
        styles={{ control: baseStyles => ({ ...baseStyles, width: '400px' }) }}
        onChange={opt => setWorkingTask({
          ...workingTask, status: opt?.value ?? 'NEW'
        })}/>
    </label> }
    { task.status === 'COMPLETE' && <div className="mt-3">
            Completed {instantToDefaultString(task.completedAt)}
    </div> }

    <label className="mt-3">
      <div>Note:</div>
      <textarea rows={2} cols={45} value={workingTask.dispositionNote} onChange={e => setWorkingTask({
        ...workingTask,
        dispositionNote: e.target.value
      })}/>
    </label>

  </div>
}

/**
 * shows a modal for editing the passed-in task.  this handles saving the task to the server.
 * If the task was saved, the updated task will be passed to the onDismiss handler
 */
export const AdminTaskEditModal = ({ task, users, onDismiss, studyEnvContext }:
                                       { task: AdminTask, users: AdminUser[], onDismiss: (task?: AdminTask) => void
                                         studyEnvContext: StudyEnvContextT}) => {
  const [workingTask, setWorkingTask] = useState<AdminTask>(task)
  const [isLoading, setIsLoading] = useState(false)

  const saveTask = () => {
    doApiLoad(async () => {
      const updatedTask = await Api.updateAdminTask(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName, workingTask)
      onDismiss(updatedTask)
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Update admin task</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <AdminTaskEditor task={task} workingTask={workingTask} setWorkingTask={setWorkingTask} users={users}/>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={saveTask}>Save</button>
        <button className="btn btn-secondary" onClick={() => onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
