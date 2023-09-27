import React, {useState} from 'react'
import {AdminTask, AdminTaskStatus, AdminUser} from "api/api";
import {Modal} from "react-bootstrap";
import Select from "react-select";
import LoadingSpinner from "util/LoadingSpinner";
import AdminUserSelect from "user/AdminUserSelect";


export default function AdminTaskEditor({task, workingTask, setWorkingTask, users}:
{ task: AdminTask, workingTask: AdminTask, setWorkingTask: (task: AdminTask) => void, users: AdminUser[]}) {
    const selectedUser = users.find(user => user.id === workingTask.assignedAdminUserId)
    const setSelectedUser = ((user: AdminUser | undefined) => {
        setWorkingTask({
            ...workingTask,
            assignedAdminUserId: user?.id
        })
    })
    const statusOpts: {label: string, value: AdminTaskStatus}[] = ['NEW', 'COMPLETE']
        .map(status => ({label: status, value: status as AdminTaskStatus}))
    const statusValue = statusOpts.find(opt => opt.value === task.status)
    return <div>
        <label>
            Status:
            <Select options={statusOpts} value={statusValue}
                    styles={{ control: baseStyles => ({ ...baseStyles, width: '400px' }) }}
                    onChange={opt => setWorkingTask({
                ...workingTask, status: opt?.value ?? 'NEW'
            })}/>
        </label>
        <label className="mt-3">
            Assigned to:
            <AdminUserSelect selectedUser={selectedUser} setSelectedUser={setSelectedUser} users={users}/>
        </label>

    </div>
}

export const AdminTaskEditModal = ({task, users, onDismiss}:
                                       { task: AdminTask, users: AdminUser[], onDismiss: () => void}) => {
    const [workingTask, setWorkingTask] = useState<AdminTask>(task)
    const [isLoading, setIsLoading] = useState(false)

    const saveTask = () => {
        alert('not yet implemented')
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
                <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
            </LoadingSpinner>
        </Modal.Footer>
    </Modal>
}